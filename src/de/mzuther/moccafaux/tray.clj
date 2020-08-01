(ns de.mzuther.moccafaux.tray
  (:require [de.mzuther.moccafaux.helpers :as helpers]
            [clojure.java.io :as io]
            [clj-systemtray.core :as tray])
  (:gen-class))


(def current-icon (ref nil))


(defn handle-menu-click
  "Handle clicks in the menu of a tray bar icon and print name of
  corresponding menu item."
  [event]
  (let [item-name (.getActionCommand ^java.awt.event.ActionEvent event)]
    (io! (newline)
         (helpers/printfln "%s  Click:    %s"
                           (helpers/get-timestamp)
                           item-name)
         (newline)
         (helpers/print-line \-))

    (condp = item-name
      "Quit" (System/exit 0)
      nil)))


(defn add-to-traybar
  "Create tray icon with an attached menu and add it to the system's
  tray bar.

  Return instance of created TrayIcon class."
  [icon-resource-path]
  (when (tray/tray-supported?)
    (dosync
      (when @current-icon
        (tray/remove-tray-icon! @current-icon)
        (ref-set current-icon nil))

      (let [icon-url (io/resource icon-resource-path)
            menu     (tray/popup-menu
                       (tray/menu-item (helpers/get-application-and-version)
                                       handle-menu-click)
                       (tray/separator)
                       (tray/menu-item "Quit"
                                       handle-menu-click))]
        (ref-set current-icon (tray/make-tray-icon! icon-url menu))))))