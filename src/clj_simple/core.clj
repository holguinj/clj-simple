(ns clj-simple.core
  (:require [clojure.string :as s]
            [clojure.walk :refer [postwalk]]
            [clj-http.client :as http]
            [clj-http.cookies :as cookies]
            [cheshire.core :as json]))

(def ^:const base-url "https://bank.simple.com")

(def json-key->clj-key
  "Given a string, replaces any underscores with dashes and then
  converts it into a keyword."
  (comp keyword #(s/replace % "_" "-")))

(defn- balance-walker
  "This function is intended to be used with postwalk to convert the
  balances returned from simple.com into dollar amounts."
  [x]
  (if (integer? x)
    (float (/ x 10000))
    x))

(defn get-csrf
  "Returns a new CSRF token from simple.com. Should (probably) be
  called with *cookie-store* bound to something that you'll keep a
  reference to."
  []
  (let [body (-> "https://bank.simple.com" http/get :body)]
    (last (re-find #"<input value=\"(.+)\" name=\"_csrf" body))))

(defprotocol ISimpleAccount
  (balances [this] "Return the current balance for this account.")
  (logout [this] "Log this account out of simple.com."))

(deftype Account [cookie-jar csrf-token]
  ISimpleAccount

  (balances [this]
    (let [resp (http/get (str base-url "/account/balances")
                         {:headers {:accept "application/json"
                                    :X-CSRF-Token (.csrf-token this)}
                          :cookie-store (.cookie-jar this)})
          balances (json/decode (:body resp) json-key->clj-key)]
      (postwalk balance-walker balances)))

  (logout [this]
    (let [resp (http/get (str base-url "/signout")
                         {:headers {:X-CSRF-Token (.csrf-token this)}
                          :cookie-store (.cookie-jar this)})]
      (if (= 200 (:status resp))
        :ok
        resp))))

(defn login
  "Constructor for the simple.com Account type. Logs into the site and
  sets the cookie/csrf token."
  [username password]
  (let [new-jar (cookies/cookie-store)
        url (str base-url "/signin")]
    (binding [clj-http.core/*cookie-store* new-jar]
      (let [token (get-csrf)
            resp (http/post url
                            {:form-params {:username username
                                           :password password
                                           :_csrf token}
                             :headers {:X-CSRF-Token token}
                             :cookie-store new-jar})]
        (if (= 200 (:status resp))
          (Account. new-jar token))))))
