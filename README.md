# Space Game 2
Recreation of a side project in java using a real  
physics engine instead of making my own.

This is to help me relearn java for my class.

https://trello.com/b/iWLwNVqD/spacegame-todo

### Theory for ship construction
Each ship has a root part which contains a list of attachment points  
and a pointer to the actual part on that attachment point. The child  
part will have to be told which attachment point is being used as well.

### How the simulation works and different terminology
- The Universe class holds all the solar systems, planets, moons, and entities. It has Box2D world where an entity will be stored when it has no nearby gravitational parent, this is equivalent to deep space. This class handles transfers between the different Box2D worlds.
- OrbitUtils contains different helper functions such as coordinate conversions to system-space to universe-space. Universe-space should be avoided because it is prone to floating point precision errors with large systems.
  
### Save file structure
- saves/settings
- saves/universes
    - saves/universes/(world_name)
        - saves/universes/(world_name)/planets/(planet_id)/
            - saves/universes/(world_name)/planets/(planet_id)/level.dat
                - chunk_width
                - chunk_height
                - atmos_radius
                - atmos_density
                - atmos_composition
                - atmos_percentages
                - seed
                - loaded_chunks
                    - x
                    - y
                - /data/
                    - chunk-x-y.dat
                        - tiles
        - saves/universes/(world_name)/ships/(ship_name).ship
        - saves/universes/(world_name)/simulation.dat
            - entities
                - classname
                - x
                - y
                - vx
                - vy
                - physworld_id
                - celestial_id
        - saves/universes/(world_name)/player.dat
            - x
            - y
            - vx
            - vy
            - physworld_id
            - celestial_id

### Creation procedure:
1. Create simulation
2. Create universe
3. Load planets
    - Load world, parent, position, and velocity
4. Load entities
    - Load position and world from files
5. Load player
    - Load position and world from files
6. Start scene

### Refactor
1. Error in universe calculations caused by sphere of influence not being correct on initialization
2. Add new render system for when on a planet
3. Rework camera system
4. Fix scaling