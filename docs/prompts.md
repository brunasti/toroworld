# Prompts

The prompts given to Claude for the Toroworld project, in order and verbatim.

## 2026-11-21

### 1. 16:34

Let's start a new project: "Toroworld"

### 2. 16:35

This project is gonna be a Java application, which simulate a torus world where animals and plants compete for resources.
I have a project directory already set up: /Users/paolobrunasti/Work/Mine/toroworld
And a git repo: https://github.com/brunasti/toroworld.git

### 3. 16:41

1 - Gradle
2 - discrete grid, with the size configurable. For the development let's start with a 10x10 grid
3 - Swing
4 

* a - time is discrete

* b - yes
* c - yes, differen kinds of activities has different costs
* d - yes, but we must define how the mutation happens
* e - yes

5 - let's work as a branch

### 4. 16:41

Please save in a "docs/prompts.md" document all the prompts I gave

### 5. 16:59

OK, as a first starting point it's OK.
1 - this is a complext thing we should think about. Could we create an option to set on or off the trade-off, and if it's on then we have a max value for all the traits value? And yes, let's make the small random changes.
2 - Don't worry about the carnivore problem for now.
3 - the package should be: it/brunasti.symulations.toroworld

* for the BRIEF.md, ok to add it to the .gitignore

### 6. 17:07

Yes, please push it, but with which branch name and commit title?

### 7. 17:15

Could you create a document docs/how-to-run.md describing how to configure and run the simulation, as for example where to set the different parameterds?

### 8. 17:19

In the bottom bar, where the current tick is displayed, please add next to it also the total number of ticks

### 9. 17:22

Set the default size of the grid to 100x50

### 10. 17:25

2

### 11. 17:30

Please start with this rate as default: 50/20/4

### 12. 17:34

How fast do the erbivorous move?

### 13. 17:39

So I don't understand the colours in the map...
What's the meaning of a black cell?
Maybe we should create a general description document (and update it after any significant decision) as a docs/manual.md document

### 14. 17:44

OK, let's add a button in the UI which open in a preview window this manual, so that when executing the application a user can read it

### 15. 17:54

OK, the manual works perfectly.

### 16. 17:57

Now let's set the carnivore sped as an evolutionary trait, which can variate between a min (1) and a max (set as a config parameter for the given simulation, let's say by default 3). This means also that the carnivore should be able to see at a distance that is at least 1.5 of their max speed (again an evolutionary trait, from a min of 1.5 to a max of 3).

### 17. 18:08

1 - OK
2 - ok, for now

### 18. 18:19

Change the colour code of the cells.
Replace the black with a medium grey, and the dark brown (1.5 res) with a light grey.
The dark brown and the black are too in contrast with the rest and distract the vision....

### 19. 18:24

Can you create a popup window where I can set the specific parameters for the next execution?

### 20. 18:57

In the resource display, could you add a breakdown per type of entity? How much in vegetables, erbivore and carnivore
