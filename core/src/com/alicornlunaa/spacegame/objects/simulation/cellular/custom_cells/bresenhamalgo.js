let cellsize = 10;
let cwidth = 40;
let cells = [];

function newCell(x, y){
  return {
      x: x,
      y: y
  };
}

function setup() {
  createCanvas(400, 400);
  
  for(let y = 0; y < cellsize; y++) for(let x = 0; x < cellsize; x++){
    cells.push(newCell(x, y));
  }
}

function calcLine(startx, starty, endx, endy, path){
  let dx = abs(endx - startx);
  let dy = abs(endy - starty);
  
  let px = startx;
  let py = starty;
  let horizontalStep = (startx < endx) ? 1 : -1;
  let verticalStep = (starty < endy) ? 1 : -1;
  let diff = dx - dy;
  
  while(true){
    let ddiff = 2 * diff;
    
    if(ddiff > -dy){
      diff -= dy;
      px += horizontalStep;
    }
    
    if(ddiff < dx){
      diff += dx;
      py += verticalStep;
    }
    
    if(px == endx && py == endy) break;
    
    path.push(newCell(px, py));
  }
}

function draw() {
  background(200);
  
  let cx = floor(mouseX / cwidth);
  let cy = floor(mouseY / cwidth);
  let selectedID = (cx + cy * cellsize);
  
  let tx = 0;
  let ty = 0;
  let targetID = (tx + ty * cellsize);
  let path = [];
  calcLine(cx, cy, tx, ty, path);
  
  for(let i = 0; i < cells.length; i++){
    let x = i % cellsize;
    let y = floor(i / cellsize);
    
    if(i == selectedID){
      fill("red");
    } else if(i == targetID){
      fill("green");
    } else {
      fill("white");
    }
    
    rect(x * cwidth, y * cwidth, cwidth, cwidth);
  }
  
  for(let i = 0; i < path.length; i++){
    let x = path[i].x;
    let y = path[i].y;

    fill("yellow");
    rect(x * cwidth, y * cwidth, cwidth, cwidth);
  }
}