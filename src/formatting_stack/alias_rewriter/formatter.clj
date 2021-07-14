(ns formatting-stack.alias-rewriter.formatter
  "A Component meant to be run via the `formatting-stack` framework."
  (:require
   [formatting-stack.alias-rewriter.api :as alias-rewriter.api]
   [formatting-stack.alias-rewriter.impl.analysis :as analysis]
   [formatting-stack.protocols.formatter :as formatter]
   [formatting-stack.util :refer [process-in-parallel!]]
   [nedap.utils.modular.api :refer [implement]]))

;; XXX [rewrite-clj.reader :as zip-reader] can be left unrewritten
;; (`reader` was already in use b/c tools.reader)
;; for that case a very good choice is removing aliasing, as long as it's safe
;; at the very least it should try picking rewrite-clj.reader (which is almost the same as removing aliasing)

(defn format! [{::keys [acceptable-aliases-whitelist]} files]
  (let [state (atom (analysis/global-project-aliases))]
    (->> files
         (process-in-parallel! (fn [filename]
                                 (let [formatting (alias-rewriter.api/rewrite-aliases filename
                                                                                      state
                                                                                      acceptable-aliases-whitelist)]
                                   (when-not (= formatting
                                                (slurp filename))
                                     (spit filename formatting)))))))
  nil)

(defn new [& {:keys [acceptable-aliases-whitelist]}]
  (implement {:id ::id
              ::acceptable-aliases-whitelist acceptable-aliases-whitelist}
    formatter/--format! format!))
