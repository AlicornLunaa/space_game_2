# Space Game 2
Recreation of a side project in java using a real  
physics engine instead of making my own.

This is to help me relearn java for my class.

https://trello.com/b/iWLwNVqD/spacegame-todo

### Theory for ship construction
Each ship has a root part which contains a list of attachment points  
Each attachment point holds a vector location in local space to the part  
and a pointer to the actual part on that attachment point. The child  
part will have to be told which attachment point is being used as well.