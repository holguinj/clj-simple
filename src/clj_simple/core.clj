(ns clj-simple.core
  (:require [clj-http.client :as http]
            [clj-http.cookies :as cookies]
            [cheshire.core :as json]
            [clj-simple.util :refer [json-key->clj-key convert-amounts get-csrf login-succeeded?]]))

(def ^:const base-url "https://bank.simple.com")

(defprotocol ISimpleAccount
  (balances [this] "Return the current balance for this account.")
  (card [this] "Returns general information about the status of the card associated with the logged-in account.")
  (logout [this] "Log this account out of simple.com."))

(deftype Account [cookie-jar csrf-token]
  ISimpleAccount

  (balances [this]
    (let [resp (http/get (str base-url "/account/balances")
                         {:headers {:accept "application/json"
                                    :X-CSRF-Token (.csrf-token this)}
                          :cookie-store (.cookie-jar this)})
          balances (json/decode (:body resp) json-key->clj-key)]
      (convert-amounts balances)))

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
        (if (login-succeeded? resp)
          (Account. new-jar token))))))

(comment
  (defn GET [acct endpoint]
    (let [url (str base-url endpoint)
          opts {:headers {:X-CSRF-Token (.csrf-token acct)}
                :cookie-store (.cookie-jar acct)}
          resp (http/get url opts)]
      (json/decode (:body resp) json-key->clj-key)))
  )
