## Complex Events Scheduler

This is the application produced for my Computer Science Tripos Part II dissertation project. It is able to automatically produce schedules for multi-day events with many constituent sessions (such as panels, matches, performances, classes, lectures, exams, etc.), using a variety of algorithms I implemented. Two of these algorithms use the constraint satisfaction library [Choco-solver](https://choco-solver.org/).

### Exhaustive Brute-Force Search

This is the first approach, which was implemented as a baseline.

### Constraint Satisfaction Approach

This is the second approach, and the one which uses Choco-solver, to model scheduling problems as CSPs and solve them with a backtracking search.
