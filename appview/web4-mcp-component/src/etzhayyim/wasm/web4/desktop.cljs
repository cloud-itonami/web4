(ns etzhayyim.wasm.web4.desktop
  "Reagent mount point for the web4 appview page."
  (:require [reagent.dom.client :as rdc]
            [etzhayyim.wasm.web4.ui :as ui]))

(defonce root (rdc/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load render! []
  (rdc/render root [ui/app-view nil]))

(defn init! []
  (render!))
