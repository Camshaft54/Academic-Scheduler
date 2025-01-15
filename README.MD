# Academic Scheduler

A Monte-Carlo solver for scheduling classes based on student requests while accounting for factors such
as class size and time conflicts.

## Example Demo

The `/input` directory contains sample course and student specifications that have been fabricated for the purpose of
demonstration. There is also a spreadsheet that can be used to create new school specifications (i.e. a `students.json`
and `courses.json`).

To try the demo, simply run `Main.kt` and it will output the results, which are as follows:

* **% of satisfied course requests**: each student will provide a list of the courses they want, ranked in order by
  priority. The percentage here is the number of individual course requests (7 per student in this example) that were
  satisfied overall.
* **Unsatisfied required courses**: these are the names of all the courses that students are required to take, but some were
  unable to register for, suggesting that more sections may need to be added or the class size should be increased.
* **Master Schedule**: This contains all the sections for all the courses arranged by period (A through G) that resulted in
  the most course requests being satisfied.
* **Student Schedules**: These are the schedules for each student in the most optimal arrangement.