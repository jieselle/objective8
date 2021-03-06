(ns objective8.unit.workflows.sign-up-test
  (:require [midje.sweet :refer :all]
            [cemerick.friend :as friend]
            [objective8.workflows.sign-up :refer :all]
            [objective8.http-api :as http-api]
            [objective8.utils :as utils]))

(fact "retain invitation information in session when signing in / signing up with redirect"
      (let [session-with-invitation {:invitation :invitation-data}]
        (authorised-redirect :user :some-url session-with-invitation)) => :authorised-response
        (provided
         (authorise (contains {:session (contains {:invitation :invitation-data})}) :user) => :authorised-response))

(fact "only usernames containing alphanumeric characters, and between 1 and 16 characters long are valid"
      (validate-username "1-a") => falsey
      (validate-username "") => falsey
      (validate-username "123456789abcdefgh") => falsey
      (validate-username "val1d") => "val1d")

(def USER_ID 1)
(def the-user {:_id USER_ID})

(fact "about user assigning user roles"
      (fact "All signed in users obtain the :signed-in role"
            (against-background
             (http-api/get-user USER_ID) => {:writer-records []})
            (roles-for-user the-user) => (contains :signed-in))

      (fact "Users that are writers for objectives are assigned the relevant roles for those objectives"
            (against-background
             (http-api/get-user USER_ID) => {:status ::http-api/success
                                             :result {:writer-records [{:objective-id 1} {:objective-id 2}]}})
            (roles-for-user the-user) => (contains #{:writer-for-1 :writer-for-2})))
