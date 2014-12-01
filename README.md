WAILAPlugins [![Build Status](http://ci.tterrag.com/job/WAILAPlugins/badge/icon)](http://ci.tterrag.com/job/WAILAPlugins/)
============

Miscellaneous WAILA plugins for various mods. Requires [ttCore](http://ci.tterrag.com/job/ttCore/).

###Currently Supported:

- Blood Magic (Ported from ImLookingAtBlood, by Pokefenn)
  - Capacity, LP, and tier of altars
  - Progress of the altar's current craft
  - Current recipe result of Chemistry set
  - Progress of chemistry set
  - Owner of master ritual stone
  - Current ritual of master ritual stone
  - Current block/item in teleposer

- Flaxbeard's Steam Power
  - Fluids in crucibles
  - Pressure percentages on gauges
  - Steam amounts in tanks
  - State of valve pipes
  
- Forestry
  - Sapling genomes
  - Pollinated leaves info
  - Current bees inside apiaries (and genome info)
  - Progress percentage of current breed
  - RF leve of machines and engines
  - Heat percentage of engines
  
- Magical Crops
  - Growth percentage and hovering icon of the product of the plant
  
- Mekanism
  - Energy amount in all energy storing blocks
  - Gas stored in gas storage blocks
  - Fluid stored in portable tanks
  - Multiplier on salination plant
  - Type of factories
  
- Pam's Harvestcraft
  - Adds a growth percentage to tree fruits
    
- RedLogic
  - Ported overlay rendering from the Project:Red plugin in WAILA
  - Info for certain gates such as Timer, Repeater, etc.
  - Strength of red alloy wires
  
###Contributing

Simply clone this project and run the usual gradle tasks, and all depended on mods will be automatically downloaded and added to your classpath. Easy!

If you are wanting to add a new plugin, add the mod yourself manually, and once you have finished I will update the server's libs.
