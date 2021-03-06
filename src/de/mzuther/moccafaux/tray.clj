;; MoccaFaux
;; =========
;; Adapt power management to changes in the environment
;;
;; Copyright (c) 2020-2021 Martin Zuther (http://www.mzuther.de/) and
;; contributors
;;
;; This program and the accompanying materials are made available under
;; the terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0.
;;
;; This Source Code may also be made available under the following
;; Secondary Licenses when the conditions for such availability set forth
;; in the Eclipse Public License, v. 2.0 are satisfied: GNU General
;; Public License as published by the Free Software Foundation, either
;; version 2 of the License, or (at your option) any later version, with
;; the GNU Classpath Exception which is available at
;; https://www.gnu.org/software/classpath/license.html.


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
  tray bar.  Add current task-states to menu and use icon located at
  icon-resource-path.

  Return instance of created TrayIcon class."
  [task-states icon-resource-path]
  (when (tray/tray-supported?)
    (dosync
      (when @current-icon
        (tray/remove-tray-icon! @current-icon)
        (ref-set current-icon nil))

      (let [icon-url   (io/resource icon-resource-path)
            states     (for [task-state task-states]
                         (str (-> task-state first name)
                              " -> "
                              (-> task-state second name)))
            menu-items (concat [(tray/menu-item (helpers/get-application-and-version) handle-menu-click)
                                (tray/separator)]

                               (for [state (sort states)]
                                 (tray/menu-item state handle-menu-click))

                               [(tray/separator)
                                (tray/menu-item "Quit" handle-menu-click)])
            menu       (apply tray/popup-menu menu-items)]
        (ref-set current-icon (tray/make-tray-icon! icon-url menu))))))
