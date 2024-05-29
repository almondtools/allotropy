Allotropy
=========
Allotropy is a junit5 test engine designed for layout testing responsive web applications. 

Features
--------
- define the devices you want to run your tests on
- setup the view
  - simply by url 
  - or as custom setup using the selenium api
- flexible test grouping
  - use inner classes (similar to nested tests in JUnit-Jupiter)
- assign each test to one or multiple devices
  - using annotations
- execute the tests grouped by view and device
  - from your IDE
  - from your build system
- execute single tests or groups of tests
  - for debugging
- jump to the test source code 
  - from your test report in your IDE
- write your own engine extensions
  - to prepare your brower
  - to provide test data
  - ...
- use your own custom annotations
  - to reduce duplicated setups

Comparison with Galen
---------------------

| Feature                  | Allotropy             | Galen                          |
| ------------------------ | --------------------- | ------------------------------ |
| Specification Language   | Java                  | Domain Specific Language       |
| Platform                 | JUnit                 | Command Line Application       |
|                          |                       | Embeddable in Java as Blackbox |
| User Interface           | IDE (Graphical)       | Batch Process                  |
| Debugging                | IDE                   | Trial and Error                |
| Assertions               | Any Assertion Library | Domain Specific Language       |
| Extensions               | Java                  | Domain Specific Meta Language  |
