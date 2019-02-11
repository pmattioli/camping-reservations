# camping-reservations
A REST API which provides 4 http endpoints for performing basic CRUD operations for managing reservations for
a camping site.

How to run on localhost:

This is a Spring Boot application, so to execute it follow these steps:

1) Clone the repository
2) Go to the newly created root directory of the project
3) Execute: mvn clean install && java -jar app/target/camping-reservations-app-0.1.0.jar
4) Once the "Application started" message has been displayed you are ready to try the API.

---

How to use on localhost:

1) Check available reservations at http://localhost:8080/v1/reservations?startDate=2018-03-01T00:00:00Z&numberOfDays=10 
(numberOfDays is optional and defaults to 30).

2) Create Reservations by submitting a JSON POST request with the below format to 
http://localhost:8080/v1/reservations

Example:
{
  "user" : {
    "first_name" : "Pablo",
    "last_name" : "Mattioli",
    "email" : "pablo@mattioli.com"
  },
  "start_date" : "2018-03-01T00:00:00Z",
  "end_date" : "2018-03-07T00:00:00Z"
}

3) Update Reservations by submitting a JSON PUT request with the below format to 
http://localhost:8080/v1/reservations
Example:
{
  "booking_id" : "1",
  "version" : "1",
  "user" : {
    "first_name" : "Pablo",
    "last_name" : "Mattioli",
    "email" : "pablo@mattioli.com"
  },
  "start_date" : "2018-03-01T00:00:00Z",
  "end_date" : "2018-03-07T00:00:00Z"
}

4) Delete Reservations by submitting a DELETE request with the below format to
http://localhost:8080/v1/reservations
Example:
{
  "booking_id" : "1",
  "version" : "1",
  "user" : {
    "first_name" : "Pablo",
    "last_name" : "Mattioli",
    "email" : "pablo@mattioli.com"
  },
  "start_date" : "2018-03-01T00:00:00Z",
  "end_date" : "2018-03-07T00:00:00Z"
}
