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