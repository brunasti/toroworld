# Design decisions

Status of each decision: **Agreed** (you decided), **Assumed** (I chose it to make progress; please review), **Open** (still to decide).

## Agreed (2026-11-21)

| # | Decision |
|---|---|
| A1 | Java application, built with **Gradle**. |
| A2 | Space is a **discrete grid** whose edges wrap (a torus). Size is configurable; development default **10x10**. |
| A3 | Viewer in **Swing**. |
| A4 | Time is **discrete** (ticks). |
| A5 | Each cell has a renewable resource that plants consume. |
| A6 | Organisms have energy. **Different activities have different costs.** At zero energy the organism dies. |
| A7 | Reproduction above an energy threshold, **with mutation**. *How* mutation works is still open (see O1). |
| A8 | Fixed random seed: runs are reproducible. |
| A9 | Work on a **branch**, not on `master`. |

## Assumed (please review)

### Build and project

| # | Assumption | Why | Alternative |
|---|---|---|---|
| B1 | Gradle **Kotlin DSL** (`build.gradle.kts`), wrapper **Gradle 9.7.1**. | Kotlin DSL has better IDE completion. Gradle 9.1+ is needed to run on JDK 25. | Groovy DSL. |
| B2 | Compiles with `--release 25` by default. Override with `-PjavaRelease=21`. The code uses only Java 21 language features. | My workspace has JDK 21, so I could only verify at level 21. | Gradle toolchains (downloads/locates the JDK automatically). |
| B3 | Package `io.github.brunasti.toroworld`. | Usual convention for a personal project on GitHub. | `eu.synergyadvancedtechnology.toroworld`, if it should carry your company. |
| B4 | JUnit 5.13.4 for tests. | Stable and certain to exist. | JUnit 6. |
| B5 | No external runtime dependencies. | Swing is in the JDK; keeps the project simple. | |
| B6 | `BRIEF.md` added to `.gitignore`. | It contains personal information and the repository may be public. | Commit it, if the repository is private and you want it versioned. |

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
| G2 | Mutation is a pluggable function `Mutation.mutate(parentGenome, random)`. The only implementation for now is `Mutation.NONE` (exact copy). |

### Behaviour and tick order

| # | Assumption | Alternative |
|---|---|---|
| T1 | Organisms see only their own cell and the 8 neighbours. | A sense range (a genome trait?). |
| T2 | Decision rules, by priority: reproduce → eat → move (herbivores prefer the neighbouring cell with the richest plant; otherwise a random free cell) → rest. | Genome-driven or learned behaviour. |
| T3 | Deciding (`Behaviour`) is separate from executing (`Simulation`), so rules can change without touching the energy bookkeeping. | |
| T4 | Tick order: resource regenerates → all plants act (random order) → all animals act (random order) → dead removed. | All organisms in one random order; simultaneous update (everyone decides on the same snapshot, then conflicts are resolved). |

## Open

### O1. How mutation happens

Several options, which can be combined:

1. **Gaussian drift per trait**: each trait changes with probability *p* by a random amount from a normal distribution (e.g. σ = 5% of the value), then is clamped to its valid range. Simple and the most common choice. *Limit:* the traits change independently.
2. **Mutation rate as a trait**: *p* and σ are themselves in the genome and can evolve. Closer to biology, but the dynamics are harder to read and to debug.
3. **Trade-offs between traits**: e.g. a higher `intake` forces a higher `metabolism`. Without trade-offs, evolution simply maximises every "good" trait, and the results become trivial.
4. **Rare large jumps**, in addition to small drift.

My suggestion: start with **1 + 3**, both with explicit parameters. Your call.

### O2. Carnivores do not survive with the current parameters

Measured with the default parameters over 10 to 20 seeds and 1,000 ticks: plants and herbivores usually coexist on 10x10, but **the carnivores never survive**. They either wipe out the herbivores and then starve, or starve directly. This holds on 20x20 and 40x40 grids too. Possible directions:

- hunting success probability, or prey that can flee;
- carnivores that sense prey at a distance (otherwise they wander at random);
- a satiety limit, so they stop hunting when full;
- calibrating the energy flows instead of guessing parameters.

### O3. Others still to decide

- Configuration from a file (YAML/properties) or only from code and command line?
- Statistics export (CSV) and charts of the populations over time?
- Should dead organisms return energy to the cell's resource?
