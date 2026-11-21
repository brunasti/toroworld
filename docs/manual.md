# Toroworld manual

A general description of the simulation: what the world is, what lives in it, the rules, and how to read the map.

> This manual describes the **current state** of the model. It is updated after every significant decision. For *why* things are the way they are, and for the open questions, see [decisions.md](decisions.md). For building, running and changing the parameters, see [how-to-run.md](how-to-run.md).
>
> Last updated: 2026-11-21 (after prompt 20).

## 1. What Toroworld is

A simulation of an ecosystem where plants and animals compete for resources. The world is a grid of cells, time advances in discrete steps (**ticks**), and every organism spends energy to live, eat, move and reproduce. Offspring inherit their parent's genome with small random changes, so species can evolve.

A run is fully **reproducible**: the same configuration and the same random seed always give the same run.

## 2. The world

- **A grid whose edges are joined.** Leaving the right edge brings you back on the left, leaving the bottom brings you back at the top. Topologically this is a torus (a doughnut), which is where the name comes from. There are no borders and no corners: every cell is equal.
- **Default size: 100 x 50 cells.**
- **Neighbours.** Each cell has 8 neighbours: the cells around it, diagonals included. Organisms only see and act on their own cell and these 8.
- **Occupancy.** A cell can hold at most **one plant** and at most **one animal** at the same time. A herbivore can stand on a plant.
- **Resource.** Each cell holds a renewable resource (think of it as nutrients and light). Every cell starts with 5, regains 1 per tick, and never goes above 10. Only plants use it.

## 3. The inhabitants

| Species | Moves? | Eats | Eaten by |
|---|---|---|---|
| **Plant** | no | the resource of its own cell | herbivores |
| **Herbivore** | yes | the plant in its own cell | carnivores |
| **Carnivore** | yes | a herbivore in a neighbouring cell | nobody |

At the start, organisms are placed at random in proportion to the grid size: **50 plants, 20 herbivores and 4 carnivores per 100 cells**. On the default grid that is 2,500 / 1,000 / 200.

## 4. Energy

Energy is the currency of life. Every organism has an energy level.

- **Gaining energy:** only by eating. A plant absorbs resource from its cell; a herbivore takes a bite of the plant it stands on; a carnivore kills a neighbouring herbivore. Only part of the food becomes energy (the efficiency): 80% for plants, 70% for herbivores, 60% for carnivores.
- **Spending energy:** every tick, an organism pays the cost of **one activity** it chose, **plus** a basal cost of living. All costs are multiplied by the organism's metabolism.

| Activity | Plant | Herbivore | Carnivore |
|---|---|---|---|
| Live (every tick) | 0.5 | 0.4 | 0.6 |
| Rest | 0 | 0 | 0 |
| Move, **per cell travelled** | (never moves) | 0.3 | 0.6 |
| Eat | 0.2 | 0.2 | 0.3 |
| Reproduce | 1.0 | 2.0 | 3.0 |

- **Death:** when its energy reaches zero, or when it grows older than its maximum age (60, 80 and 120 ticks for plants, herbivores and carnivores).

## 5. One tick, step by step

1. Every cell regains resource.
2. Every plant acts, in random order.
3. Every animal acts, in random order.
4. Dead organisms are removed.

"Acting" means: choose an activity, carry it out, pay its cost plus the cost of living, then grow one tick older. Costs are paid **after** the action, so a starving organism can still eat and survive. Organisms born during a tick start acting on the next tick.

## 6. How organisms decide

The rules are deliberately simple. In order of priority:

1. **Reproduce**, if energy is above the reproduction threshold and a neighbouring cell is free.
2. **Eat**, if food is within reach: a plant in its own cell for a herbivore, a herbivore in a neighbouring cell for a carnivore.
3. **Hunt** (carnivores only). If a herbivore is within sight, the carnivore runs towards the nearest one, up to its speed in cells. If it gets next to the prey, it attacks in the same tick (a **pounce**). Otherwise it has simply moved closer.
4. **Move** one cell (animals only). A herbivore steps to the free neighbouring cell with the most energetic plant. A carnivore with no prey in sight, or a herbivore with no plant nearby, steps to a random free cell.
5. **Rest**, if nothing else is possible.

### Speed and sight

| | Plants | Herbivores | Carnivores |
|---|---|---|---|
| Speed (cells per tick) | 0 (never move) | 1, fixed | **evolves** from 1 to 3 (start: 1) |
| Sight | own cell | the 8 neighbours | **evolves**: vision × speed cells (start: 1.5 × 1 = 1 cell) |

- **Speed** is a genome trait. A carnivore with speed 2 travels up to 2 cells per tick. A fractional speed gives a matching chance of one more cell: speed 1.3 means 1 cell, plus a second one 30% of the time.
- **Vision** is a genome trait, from 1.5 to 3. The sight distance in cells is vision × speed, rounded down: a carnivore with speed 2 and vision 1.5 sees 3 cells away. So faster carnivores also see further.
- How sight is computed is part of each species' description. The alternative rule, already available, is vision × the species' *maximum* speed, so that sight does not depend on the individual's speed. This keeps the model open to different kinds of carnivore in the future.
- Running costs the move cost **for every cell travelled**, so speed is useful but expensive.
- Movement is one cell at a time, diagonals included. Distances are measured the same way: the number of such steps.

## 7. Reproduction

- **Asexual, one offspring at a time,** placed in a free neighbouring cell.
- The parent pays the reproduction cost, then gives **40%** of its remaining energy to the offspring.
- Reproduction thresholds: 8 (plants), 25 (herbivores), 30 (carnivores).

## 8. Genome, mutation and the trade-off

Every organism carries its own **genome** of seven traits:

| Trait | Meaning | Kind |
|---|---|---|
| metabolism | multiplier on all activity costs | advantage when **lower** |
| intake | maximum food per meal | advantage when **higher** |
| maxAge | maximum age in ticks | advantage when **higher** |
| reproductionThreshold | energy needed to reproduce | strategy |
| offspringShare | share of energy given to the offspring | strategy |
| speed | cells travelled per tick (carnivores only, 1 to 3) | advantage when **higher** |
| vision | sight as a multiple of speed (carnivores only, 1.5 to 3) | advantage when **higher** |

**Mutation (on by default).** At birth, each trait of the offspring has a 10% chance of changing by a small random amount, typically about 5% of its value.

**Trade-off (on by default).** A species cannot improve everything at once. Each genome gets an *advantage* score measured against its species' founder genome (the starting one), counting only the advantage traits (metabolism, intake, max age, speed, vision). The score cannot go above zero, so a better intake, or a faster carnivore, must be paid for with, for example, a higher metabolism or a shorter life. A mutation that would break this rule is discarded, and the offspring is an exact copy of its parent. The two strategy traits are not part of the budget and drift freely.

## 9. Reading the map

![Map legend](images/legend.png)

**Cell background: the resource left in the cell.** It goes from light brown (full, 10), through beige (half full, 5) and light grey (low, 1.5), to **medium grey** (empty, 0). The greys were chosen to stay calm: an exhausted cell should be visible without drawing the eye away from the organisms.

> **A medium grey cell is an empty cell whose resource is used up.** It has no plant and no animal, and it has nothing left to feed a new plant yet. It regains 1 per tick: it turns light grey within a couple of ticks, then back towards brown, unless a plant keeps draining it.

The colours are:

| What you see | Meaning |
|---|---|
| Light brown → beige | Cell with plenty of resource |
| Light grey → medium grey | Cell with little or no resource left |
| Green square, dark → bright | Plant. Brighter means more energy, closer to reproducing. |
| Blue circle, white outline | Herbivore |
| Red circle, white outline | Carnivore |

On the default 100 x 50 grid each cell is only 8 pixels wide, so a plant fills almost the whole cell and the background shows only as a thin border. Grey cells are usually cells that a plant drained before it died.

**Buttons**, at the top of the window: **Start/Pause**, **Step** (one tick), **Reset**, the **Speed** slider (ticks per second), **Settings...**, and **Manual**, which opens this manual in a separate window.

**Settings...** opens a window where you can change every parameter of the simulation: the world (size, seed, length of the run, resource), each species (starting density and energy, founder genome, activity costs, and for carnivores speed and vision), and mutation. **Save and start new run** saves them and starts a new run; they are also used at the next start of the application. The details are in [how-to-run.md](how-to-run.md), section 5.

**Status bar**, at the bottom of the window:
- line 1: current tick and total ticks of the run (e.g. `Tick 15 / 1000`, with "(end)" at the last tick), and the number of plants, herbivores and carnivores;
- line 2: where the energy is. **Resource in cells** is the total resource left in all the cells, not yet taken by plants. **Energy in plants / herbivores / carnivores** is the total energy held by the living organisms of each species. Together they show how energy flows up the food chain: from the cells to the plants, to the herbivores, to the carnivores;
- line 3: grid size, random seed, and whether mutation and the trade-off are on.

## 10. What to expect from the current defaults

The parameters are a first guess, not a calibration. With the defaults:

- **Plants** quickly cover almost the whole grid.
- **Herbivores** crash from 1,000 to about 10–20 within the first 100 ticks. Only a handful are left at tick 1,000, so in some runs they may die out.
- **Carnivores** die out within the first 100 ticks: they first eat most of the herbivores, then starve. Being able to hunt at a distance has not changed this yet: they die before speed and vision have time to evolve.

Balancing the carnivores is a known open question (O2 in [decisions.md](decisions.md)), parked for now.
