# clj-simple

**Pre-alpha**: this library is experimental, and shouldn't be used for anything remotely important. I have no reason to think it's dangerous in any way, but use it at your own risk.

This is a Clojure library for accessing the (undocumented) [simple.com](https://www.simple.com) API.

I created this library because I wanted to keep track of my safe-to-spend balance my own way, without having to manually log in to the site all the time.

My thanks goes to wearefractal for the ["bank"](https://github.com/wearefractal/bank) library, which gave me a good sense of how to get started with this project.

## Usage

This library isn't on Clojars yet, so install it by running `lein install` from this directory.

There's barely any functionality here so far, so I'll share this brief code sample with you:

```clojure
(ns my-simple-app.core
  (:require [clj-simple.core :as simple]))

;; the login function returns a new account object
(def my-account
  (simple/login "my-username" "my-secret-password")))

;; to check the balances associated with the given account:
(simple/balances my-account)
;;=> {:total 666.42, :safe-to-spend 100.0, :bills 25.0, :deposits 0.0, :pending 12.63, :goals 200.12}

;; logging out when you're done is a good idea, probably:
(simple/logout my-account)
```

## Limitations

Right now this doesn't do much other than check the balances on an account. I'll probably add some more features soon.

I mostly reverse-engineered this stuff from the HTML source on Simple's web site. Simple doesn't document or support third-party use of their API, so there's a good chance it will suddenly stop working at some point.

## License

Copyright © 2014 Justin Holguin

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
