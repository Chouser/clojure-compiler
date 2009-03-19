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

(defmacro with-compiler-ns [& body]
  `(binding [*ns* ~*ns*] ~@body))

(deftest host-expr-types
  (is (= :static-method   (:type (analyze '(. System gc)))))
  (is (= :static-method   (:type (analyze '(. System (gc))))))
  (is (= :static-method   (:type (analyze '(System/gc)))))
  (is (= :static-field    (:type (analyze '(. Integer MAX_VALUE)))))
  (is (= :static-field    (:type (analyze '(Integer/MAX_VALUE)))))
  (is (= :static-field    (:type (analyze 'Integer/MAX_VALUE))))
  (is (= :instance-method (:type (analyze '(. 5 byteValue)))))
  (is (= :instance-method (:type (analyze '(. 5 (equals 5))))))
  (is (= :instance-method (:type (analyze '(. 5 (foo 5))))))
  (is (= :instance-method (:type (analyze '(.equals 5 5)))))
  (is (= :instance-method (:type (analyze '(.foo 5 5)))))
  (is (= :instance-field  (:type (analyze '(. map foo)))))
  (is (= :instance-field  (:type (analyze '(. 5 byteValue))))))


(import '(java.io PushbackReader BufferedReader LineNumberReader)
        '(clojure.lang LineNumberingPushbackReader))

(deftest auto-hint
  (is (= #{BufferedReader LineNumberingPushbackReader}
         (set (map #(.getDeclaringClass %)
                   (:methods (with-compiler-ns
                               (analyze '(.readLine map)))))))))
