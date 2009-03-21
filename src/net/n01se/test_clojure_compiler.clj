;;  Copyright (c) Chris Houser. All rights reserved. The use and
;;  distribution terms for this software are covered by the Common Public
;;  License 1.0 (http://opensource.org/licenses/cpl.php) which can be found
;;  in the file CPL.TXT at the root of this distribution. By using this
;;  software in any fashion, you are agreeing to be bound by the terms of
;;  this license. You must not remove this notice, or any other, from this
;;  software.
;;
;;  Tests for the self-hosted Clojure compiler
;;
;;  by Chouser, http://chouser.n01se.net
;;  Created Mar 2009

(ns net.n01se.test-clojure-compiler
  (:use clojure.contrib.test-is
        [net.n01se.clojure-compiler :only (analyze)]))

(defn ok-map [e t]
  (every? identity (map (fn [[k v]] (= (t k) v)) e)))

(deftest host-expr-types
  (is (= :static-method   (:type (analyze '(. System gc)))))
  (is (= :static-method   (:type (analyze '(. System (gc))))))
  (is (= :static-method   (:type (analyze '(System/gc)))))
  (is (= :static-field    (:type (analyze '(. Integer MAX_VALUE)))))
  (is (= :static-field    (:type (analyze '(Integer/MAX_VALUE)))))
  (is (= :static-field    (:type (analyze 'Integer/MAX_VALUE))))
  (is (ok-map {:allow-field false} (analyze '(. 5 byteValue))))
  (is (ok-map {:allow-field false} (analyze '(. 5 (equals 5)))))
  (is (ok-map {:allow-field false} (analyze '(. 5 (foo 5)))))
  (is (ok-map {:allow-field false} (analyze '(.equals 5 5))))
  (is (ok-map {:allow-field false} (analyze '(.foo 5 5))))
  (is (ok-map {:allow-field true :members #{}} (analyze '(. map foo))))
  (is (ok-map {:allow-field false} (analyze '(. 5 byteValue)))))


(import '(java.io PushbackReader BufferedReader LineNumberReader)
        '(clojure.lang LineNumberingPushbackReader))

(defmacro with-compiler-ns [& body]
  `(binding [*ns* ~*ns*] ~@body))

(defn target-classes [form]
  (set (map #(.getDeclaringClass %)
            (:members (with-compiler-ns
                        (analyze form))))))

(deftest auto-hint
  (is (= #{Object}
         (target-classes '(.equals 5 5))))
  (is (= #{BufferedReader LineNumberingPushbackReader}
         (target-classes '(.readLine map))))
  (is (ok-map {:java-class Long/TYPE} (analyze '(Math/round 3.4))))
         )
