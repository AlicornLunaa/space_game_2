# Parts layout guide
This file shows the layout for a part with JSON.  
The attachmentPoints field defines where objects  
will snap together at.  
The metadata field depends on the type of part it is.

## All:
```json
{
    "id": "PART_ID",
    "type": "PART_TYPE",
    "name": "PART_NAME",
    "desc": "PART_DESCRIPTION",
    "scale": 1,
    "interiorSize": 0,
    "attachmentPoints": [
        {"x": 8, "y": 0}
    ],
    "freeform": false,
    "metadata": {}
}
```

## Thrusters:
```json
"metadata": {
    "power": 1000,
    "cone": 10,
    "fuelUsage": 1,
    "effect": "effects/rocket"
}
```

## Structural
```json
"metadata": {
    "fuelCapacity": 1000,
    "batteryCapacity": 1000
}
```

## Aero:
```json
"metadata": {
    "drag": 1,
    "lift": 1
}
```

## RCSPort:
```json
"metadata": {
    "power": 100,
    "fuelUsage": 0.1
}
```

## ReactionWheel:
```json
"metadata": {
    "power": 100,
    "batteryUsage": 1
}
```

## SolarPanel:
```json
"metadata": {
    "chargeRate": 100
}
```

## NuclearReactor:
```json
"metadata": {
    "chargeRate": 1000,
    "fuelUsage": 1
}
```

## EnvironmentControl
```json
"metadata": {
    "oxygenRate": 100,
    "heatRate": 100
}
```

## Cargo
```json
"metadata": {
    "cargoType": "CARGO_TYPE",
    "size": 16
}
```