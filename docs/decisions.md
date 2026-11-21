# Design decisions

Status of each decision: **Agreed** (you decided), **Assumed** (I chose it to make progress; please review), **Open** (still to decide).

## Agreed (2026-11-21)

| # | Decision |
|---|---|
| A1 | Java application, built with **Gradle**. |
| A2 | Space is a **discrete grid** whose edges wrap (a torus). Size is configurable. Default **100x50** (10x10 before prompt 9). |
| A3 | Viewer in **Swing**. |
| A4 | Time is **discrete** (ticks). |
| A5 | Each cell has a renewable resource that plants consume. |
| A6 | Organisms have energy. **Different activities have different costs.** At zero energy the organism dies. |
| A7 | Reproduction above an energy threshold, **with mutation**. |
| A8 | Fixed random seed: runs are reproducible. |
| A9 | Work on a **branch**, not on `master`. |
| A10 | Package `it.brunasti.simulations.toroworld`. |
| A11 | `BRIEF.md` stays out of version control (`.gitignore`). |
| A12 | Mutation by **small random changes** to the traits. |
| A13 | A **trade-off** option, on or off. When on, all the traits share **one maximum budget**: improving one trait must be paid for by another. |
| A14 | The carnivore survival problem (O2) is parked for now. |
| A15 | Starting populations are **densities** (fractions of the cells), so they follow the grid size. Defaults: plants 0.30, herbivores 0.10, carnivores 0.02. Counts are rounded to the nearest whole organism. |

## Assumed (please review)

### Build and project

| # | Assumption | Why | Alternative |
|---|---|---|---|
| B1 | Gradle **Kotlin DSL** (`build.gradle.kts`), wrapper **Gradle 9.7.1**. | Kotlin DSL has better IDE completion. Gradle 9.1+ is needed to run on JDK 25. | Groovy DSL. |
| B2 | Compiles with `--release 25` by default. Override with `-PjavaRelease=21`. The code uses only Java 21 language features. | My workspace has JDK 21, so I could only verify at level 21. | Gradle toolchains (downloads/locates the JDK automatically). |
| B4 | JUnit 5.13.4 for tests. | Stable and certain to exist. | JUnit 6. |
| B5 | No external runtime dependencies. | Swing is in the JDK; keeps the project simple. | |

### World

| # | Assumption | Alternative |
|---|---|---|
| W1 | **Moore neighbourhood**: 8 surrounding cells. Distance is Chebyshev (king moves). | Von Neumann (4 cells). |
| W2 | A cell holds **at most one plant and at most one animal** at a time. | Several organisms per cell, with a capacity. |
| W3 | Resource: every cell starts at 5, regenerates 1 per tick, maximum 10. Uniform for all cells. | Uneven terrain, seasons, resource released by dead organisms. |
| W4 | Three species: **plant, herbivore, carnivore**. Herbivores eat plants; carnivores eat herbivores. | More species, omnivores, carnivores eating carnivores. |

### Organisms and energy

| # | Assumption | Alternative |
|---|---|---|
| E1 | Activities: `LIVE` (basal, paid every tick), `REST`, `MOVE`, `EAT`, `REPRODUCE`. Each tick an organism chooses **one** activity and pays its cost **plus** `LIVE`. | Several actions per tick with an energy/time budget. |
| E2 | Costs are configured **per species**. The cost paid is `base cost × metabolism`, where metabolism is a genome trait. | Costs that depend on body size, distance, terrain. |
| E3 | Costs are paid **after** the action. A starving organism can still eat and survive. | Pay first: it dies before eating. |
| E4 | Food converts to energy with a per-species **efficiency** (plants 0.8, herbivores 0.7, carnivores 0.6). | |
| E5 | Plants absorb up to `intake` resource from their own cell. Herbivores take up to `intake` energy from the plant in their own cell; the plant dies if its energy reaches 0. **Carnivores kill** an adjacent herbivore and gain up to `intake` of its energy. | Partial attacks, hunting success probability, prey escaping. |
| E6 | Death at zero energy, or when age exceeds `maxAge` (a genome trait). | No ageing. |
| E7 | Reproduction: when `energy ≥ reproductionThreshold` and a neighbouring cell is free, the parent pays the reproduction cost and gives a share (`offspringShare`, 40%) of its remaining energy to one offspring. **Asexual**, one offspring. | Sexual reproduction (two parents, genome crossover), litters. |
| E8 | Offspring act from the tick after their birth. | |

### Genome

| # | Assumption |
|---|---|
| G1 | Genome traits: `metabolism`, `intake`, `reproductionThreshold`, `offspringShare`, `maxAge`. Every organism carries its own genome, so mutation can later act on each trait. |
| G2 | Mutation is pluggable (`Mutation`); the settings (`MutationSettings`) choose the implementation. Default: **on, trade-off on**. Command line: `--mutation on|off`, `--trade-off on|off`. |
| G3 | **Small random changes** (`GaussianMutation`): at birth each trait, independently, changes with probability **10%** by a relative amount drawn from a normal distribution with σ = **5%**: `new = clamp(value × (1 + N(0, 0.05)))`. Each trait has a valid range (`Trait`) and is clamped to it. |
| G4 | **Trade-off budget** (`TraitBudget`). The *advantage* of a genome is measured against the **founder** genome of its species (the starting genome in the configuration), as a sum of log-ratios: `ln(intake/intake₀) + ln(maxAge/maxAge₀) − ln(metabolism/metabolism₀)`. Logarithms make traits with different scales comparable: doubling the intake is worth as much as halving the metabolism. A genome is allowed when its advantage is ≤ the **allowance** (default **0**: the founders sit exactly on the limit). The budget is measured against the founder, not the parent, so it cannot creep upwards over generations. |
| G5 | `reproductionThreshold` and `offspringShare` are **strategies**, not advantages: neither higher nor lower is better in itself. They are outside the budget and drift freely. |
| G6 | When a mutated genome exceeds the budget, the mutation is **rejected** and the offspring gets the parent's genome. *Alternative:* repair it, by worsening the other traits until the genome fits. Rejection is simpler and does not invent changes that did not happen. |
| G7 | A trait whose founder value is 0 (e.g. intake 0) stays 0 (the change is relative) and is left out of the budget. |

### Behaviour and tick order

| # | Assumption | Alternative |
|---|---|---|
| T1 | Organisms see only their own cell and the 8 neighbours. | A sense range (a genome trait?). |
| T2 | Decision rules, by priority: reproduce → eat → move (herbivores prefer the neighbouring cell with the richest plant; otherwise a random free cell) → rest. | Genome-driven or learned behaviour. |
| T3 | Deciding (`Behaviour`) is separate from executing (`Simulation`), so rules can change without touching the energy bookkeeping. | |
| T4 | Tick order: resource regenerates → all plants act (random order) → all animals act (random order) → dead removed. | All organisms in one random order; simultaneous update (everyone decides on the same snapshot, then conflicts are resolved). |
| T5 | A run has a fixed length (`--ticks`, default **1000**, the same in the viewer and headless). The viewer shows `Tick n / total` and pauses at the end. Before this, the viewer ran without limit and headless defaulted to 100 ticks. | Viewer without limit unless `--ticks` is given. |

## Open

### O1. How mutation happens: **decided** (A12, A13, G2 to G7)

First observation (one seed, 20x20, 3,000 ticks, not a conclusion): with the trade-off **on**, plants evolved a lower metabolism, a lower intake and a much lower reproduction threshold (8.0 → 5.3), and plants and herbivores were still coexisting at the end. With the trade-off **off**, the world died out at tick 1,466. This needs more seeds before it means anything.

### O2. Carnivores do not survive with the current parameters (parked)

Measured with the default parameters over 10 to 20 seeds and 1,000 ticks: plants and herbivores usually coexist on 10x10, but **the carnivores never survive**. They either wipe out the herbivores and then starve, or starve directly. This holds on 20x20 and 40x40 grids too. Possible directions:

- hunting success probability, or prey that can flee;
- carnivores that sense prey at a distance (otherwise they wander at random);
- a satiety limit, so they stop hunting when full;
- calibrating the energy flows instead of guessing parameters.

### O3. Others still to decide

- Configuration from a file (YAML/properties) or only from code and command line?
- Statistics export (CSV) and charts of the populations over time?
- Should dead organisms return energy to the cell's resource?

### O4. Starting populations on the 100x50 grid: **decided** (A15)

The fixed counts chosen for 10x10 (30 / 10 / 2) were very sparse on 100x50: in 3 of 3 seeds the herbivores died out before tick 100. Populations are now densities, which on 100x50 gives 1,500 / 500 / 100.
