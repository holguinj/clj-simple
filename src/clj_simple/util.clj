(ns clj-simple.util
  (:require [clj-http.client :as http]
            [clojure.string :as s]
            [clojure.walk :refer [postwalk]]))

(def json-key->clj-key
  "Given a string, replaces any underscores with dashes and then
  converts it into a keyword."
  (comp keyword #(s/replace % "_" "-")))

(defn balance-walker
  "This function is intended to be used with postwalk to convert the
  balances returned from simple.com into dollar amounts."
  [x]
  (if (integer? x)
    (float (/ x 10000))
    x))

(defn convert-amounts
  [balances]
  (postwalk balance-walker balances))

(defn get-csrf
  "Returns a new CSRF token from simple.com. Should (probably) be
  called with *cookie-store* bound to something that you'll keep a
  reference to."
  []
  (let [body (-> "https://bank.simple.com" http/get :body)]
    (last (re-find #"<input value=\"(.+)\" name=\"_csrf" body))))

(defn login-succeeded?
  [{:keys [status body]}]
  (and (= 200 status)
       (not (re-find #"Your username and passphrase don't match" body))))
