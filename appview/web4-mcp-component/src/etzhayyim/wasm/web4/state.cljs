(ns etzhayyim.wasm.web4.state
  "UI state for the web4 appview surface (static app metadata card).")

(def app
  {:title "Web4 Mcp Component"
   :project "etzhayyim-project-web4"
   :name "web4-mcp-component"
   :kind "appview"
   :route-count 0
   :routes []
   :vars []
   :xrpc true
   :relative-path "60-apps/etzhayyim-project-web4/appview/web4-mcp-component/svelte/src/routes/+page.svelte"})

(defonce ^:private db (atom {:view :app}))

(defn current-view [] @db)

(defn set-view! [v]
  (swap! db assoc :view v))
