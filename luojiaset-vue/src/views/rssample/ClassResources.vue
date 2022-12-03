<template>
  <div class="area">
    <a-tabs default-active-key="1" @change="tabChange">
      <a-tab-pane key="1" :tab="$t('scene_class_system')">
        <div class="charts" id="scene-main"></div>
      </a-tab-pane>
      <a-tab-pane key="2" :tab="$t('object_class_system')">
        <div class="charts" id="object-main"></div>
      </a-tab-pane>
      <a-tab-pane key="3" :tab="$t('pixel_class_system')">
        <div class="charts" id="landCover-main"></div>
      </a-tab-pane>

    </a-tabs>
  </div>



</template>

<script>
import * as echarts from 'echarts';


export default {
  name: "ClassResources",
  data() {
    return {
      data_scene: {
        name: 'scene class',
        children: [
          {
            name: 'Transportation',
            children: [
              {
                name: 'Transport Carrier',
                children: [
                  { name: 'Airplane', value: 'Airplane' },
                  { name: 'Ship', value: 'Ship' }
                ]
              },
              {
                name: 'Means of Transportation',
                children: [
                  { name: 'Airport', value: 'Airport' },
                  { name: 'Airport Runway', value: 'Airport Runway' },
                  { name: 'Runway Marking', value: 'Runway Marking' },
                  { name: 'Apron', value: 'Apron' },
                  { name: 'Helipad', value: 'Helipad' },
                  { name: 'Harbor', value: 'Harbor' },
                  { name: 'Ferry Terminal', value: 'Ferry Terminal' },
                  { name: 'Railway', value: 'Railway' },
                  { name: 'Railway Station', value: 'Railway Station' },
                  { name: 'Parking Lot', value: 'Parking Lot' },
                  { name: 'Parking Space', value: 'Parking Space' },
                  { name: 'City Avenue', value: 'City Avenue' },
                  { name: 'Avenue', value: 'Avenue' },
                  { name: 'Crosswalk', value: 'Crosswalk' },
                  { name: 'Footbridge', value: 'Footbridge' },
                  { name: 'Overpass', value: 'Overpass' },
                  { name: 'Cross River Bridge', value: 'Cross River Bridge' },
                  { name: 'Viaduct', value: 'Viaduct' },
                  { name: 'Fork Road', value: 'Fork Road' },
                  { name: 'Roundabout', value: 'Roundabout' },
                  { name: 'Intersection', value: 'Intersection' },
                  { name: 'Closed Road', value: 'Closed Road' },
                  { name: 'Turning Circle', value: 'Turning Circle' },
                  { name: 'Freeway', value: 'Freeway' },
                  { name: 'Mountain Road', value: 'Mountain Road' },
                  { name: 'Bridge', value: 'Bridge' },
                  { name: 'City Road', value: 'City Road' }
                ]
              }
            ]
          },
          {
            name: 'Desert&Bare Land',
            children: [
              {
                name: 'Salinity Surface',
                children: [{ name: 'Salines', value: 'Salines' }]
              },
              {
                name: 'Sandy Surface',
                children: [{ name: 'Desert', value: 'Desert' }]
              },
              {
                name: 'Rock Surface',
                children: [{ name: 'Bare Rock', value: 'Bare Rock' }]
              }
            ]
          },
          {
            name: 'Arable Land',
            children: [
              {
                name: 'Farmland',
                children: [
                  { name: 'Paddy Field', value: 'Paddy Field' },
                  { name: 'Dry Cropland', value: 'Dry Cropland' },
                  { name: 'Irregular Farmland', value: 'Irregular Farmland' },
                  { name: 'Circular Farmland', value: 'Circular Farmland' },
                  { name: 'Rectangular Farmland', value: 'Rectangular Farmland' },
                  { name: 'Irrigated Land', value: 'Irrigated Land' },
                  { name: 'Non-irrigated Land', value: 'Non-irrigated Land' },
                  { name: 'Permanent Crops Land ', value: 'Permanent Crops Land ' },
                  { name: 'Annual Crops Land', value: 'Annual Crops Land' },
                  {
                    name: 'Complex Cultivation Patterns Land',
                    value: 'Complex Cultivation Patterns Land'
                  },
                  {
                    name: 'Annual Crops Associated With Permanent Crops Land',
                    value: 'Annual Crops Associated With Permanent Crops Land'
                  },
                  { name: 'Terraced Field', value: 'Terraced Field' },
                  { name: 'Christmas Tree Ranch', value: 'Christmas Tree Ranch' },
                  {
                    name: 'Agro-pastoral Mixed Area',
                    value: 'Agro-pastoral Mixed Area'
                  },
                  { name: 'Agroforestry Area', value: 'Agroforestry Area' },
                  { name: 'Barren Land', value: 'Barren Land' },
                  { name: 'Green Farmland', value: 'Green Farmland' }
                ]
              },
              {
                name: 'Green Land',
                children: [
                  { name: 'Vineyards', value: 'Vineyards' },
                  { name: 'Vegetable Plot', value: 'Vegetable Plot' },
                  { name: 'Olive Groves', value: 'Olive Groves' },
                  { name: 'Coffee', value: 'Coffee' },
                  { name: 'Non-coffee', value: 'Non-coffee' }
                ]
              }
            ]
          },
          {
            name: 'Woodland&Grass',
            children: [
              {
                name: 'Woodland',
                children: [
                  {
                    name: 'Natural Dense Forest Land',
                    value: 'Natural Dense Forest Land'
                  },
                  {
                    name: 'Natural Sparse Forest Land',
                    value: 'Natural Sparse Forest Land'
                  },
                  {
                    name: 'Artificial Dense Forest Land',
                    value: 'Artificial Dense Forest Land'
                  },
                  {
                    name: 'Artificial Sparse Forest Land',
                    value: 'Artificial Sparse Forest Land'
                  },
                  { name: 'Chaparral', value: 'Chaparral' },
                  { name: 'Sparse Forest', value: 'Sparse Forest' },
                  { name: 'Dense Forest', value: 'Dense Forest' },
                  { name: 'Coniferous Forest', value: 'Coniferous Forest' },
                  {
                    name: 'Sclerophyllous Vegetation',
                    value: 'Sclerophyllous Vegetation'
                  },
                  { name: 'Broad-leaved Forest', value: 'Broad-leaved Forest' },
                  { name: 'Mixed Forest', value: 'Mixed Forest' },
                  { name: 'Shrub Land', value: 'Shrub Land' },
                  { name: 'Young Growth', value: 'Young Growth' },
                  { name: 'Mangrove', value: 'Mangrove' },
                  {
                    name: 'Tropical Logging Rainforest',
                    value: 'Tropical Logging Rainforest'
                  },
                  {
                    name: 'Affected Forest Land (Strong Wind)',
                    value: 'Affected Forest Land (Strong Wind)'
                  },
                  {
                    name: 'River Protection Forest',
                    value: 'River Protection Forest'
                  },
                  { name: 'Flowering Vegetation', value: 'Flowering Vegetation' },
                  { name: 'Herbaceous Vegetation', value: 'Herbaceous Vegetation' },
                  { name: 'City Green Tree', value: 'City Green Tree' }
                ]
              },
              {
                name: 'Grass',
                children: [
                  { name: 'Natural Meadow', value: 'Natural Meadow' },
                  { name: 'Artificial Meadow', value: 'Artificial Meadow' }
                ]
              }
            ]
          },
          {
            name: 'Waters',
            children: [
              {
                name: 'Water',
                children: [
                  { name: 'Graff', value: 'Graff' },
                  { name: 'Stream', value: 'Stream' },
                  { name: 'River', value: 'River' },
                  { name: 'Pond', value: 'Pond' },
                  { name: 'Lake', value: 'Lake' },
                  { name: 'Reservoir', value: 'Reservoir' },
                  { name: 'Sea', value: 'Sea' },
                  { name: 'Beach', value: 'Beach' },
                  { name: 'Sandbeach', value: 'Sandbeach' },
                  { name: 'Hirst', value: 'Hirst' },
                  { name: 'Coastal Lagoons', value: 'Coastal Lagoons' },
                  { name: 'Intertidal Flats', value: 'Intertidal Flats' },
                  { name: 'Estuaries', value: 'Estuaries' },
                  { name: 'Lakeshore', value: 'Lakeshore' },
                  { name: 'Coast Line', value: 'Coast Line' },
                  { name: 'Sea Ice', value: 'Sea Ice' }
                ]
              },
              {
                name: 'Wetland',
                children: [
                  { name: 'Peatbogs', value: 'Peatbogs' },
                  { name: 'Salt Marshes', value: 'Salt Marshes' },
                  { name: 'Inland Marshes', value: 'Inland Marshes' }
                ]
              }
            ]
          },
          {
            name: 'Structures',
            children: [
              {
                name: 'Impervious Surface',
                children: [{ name: 'Square', value: 'Square' }]
              },
              {
                name: 'Hydraulic Facilities',
                children: [{ name: 'Dam', value: 'Dam' }]
              },
              {
                name: 'Industrial Facilities',
                children: [
                  {
                    name: 'Blue Structured Factory Building',
                    value: 'Blue Structured Factory Building'
                  },
                  {
                    name: 'Red Structured Factory Building',
                    value: 'Red Structured Factory Building'
                  },
                  {
                    name: 'Scattered Blue Roof Factory Building',
                    value: 'Scattered Blue Roof Factory Building'
                  },
                  {
                    name: 'Scattered Red Roof Factory Building',
                    value: 'Scattered Red Roof Factory Building'
                  },
                  { name: 'Refinery', value: 'Refinery' },
                  { name: 'Thermal Power Station', value: 'Thermal Power Station' },
                  { name: 'Wind Power Station', value: 'Wind Power Station' },
                  { name: 'Solar Power Station', value: 'Solar Power Station' },
                  { name: 'Steelworks', value: 'Steelworks' },
                  { name: 'Transformer Station', value: 'Transformer Station' },
                  { name: 'Oil Tank', value: 'Oil Tank' },
                  { name: 'Storage Tank', value: 'Storage Tank' },
                  { name: 'Container Yard', value: 'Container Yard' },
                  { name: 'Storage Land', value: 'Storage Land' },
                  { name: 'Pipeline', value: 'Pipeline' }
                ]
              },
              {
                name: 'Sports Facilities',
                children: [
                  { name: 'Baseball Field', value: 'Baseball Field' },
                  { name: 'Basketball Court', value: 'Basketball Court' },
                  { name: 'Golf Course', value: 'Golf Course' },
                  { name: 'Football Field', value: 'Football Field' },
                  { name: 'Tennis Court', value: 'Tennis Court' },
                  { name: 'Gym', value: 'Gym' },
                  { name: 'Stadium', value: 'Stadium' },
                  { name: 'Playground', value: 'Playground' },
                  { name: 'Ground Track Field', value: 'Ground Track Field' },
                  {
                    name: 'Sport And Leisure Facilities',
                    value: 'Sport And Leisure Facilities'
                  },
                  { name: 'Swimming Pool', value: 'Swimming Pool' }
                ]
              },
              { name: 'Greenhouse', children: [{ name: NaN, value: NaN }] },
              {
                name: 'Environmental Protection Facilities',
                children: [{ name: 'Dump Sites', value: 'Dump Sites' }]
              },
              {
                name: 'Sewage Treatment Plant',
                children: [
                  {
                    name: 'Class I Sewage Treatment Plant',
                    value: 'Class I Sewage Treatment Plant'
                  },
                  {
                    name: 'Class II Sewage Treatment Plant',
                    value: 'Class II Sewage Treatment Plant'
                  },
                  { name: 'Waste Water Tank', value: 'Waste Water Tank' }
                ]
              }
            ]
          },
          {
            name: 'Other',
            children: [
              {
                name: 'Nature',
                children: [
                  { name: 'Mountain', value: 'Mountain' },
                  { name: 'Snowberg', value: 'Snowberg' },
                  { name: 'Island', value: 'Island' },
                  { name: 'Cloud', value: 'Cloud' }
                ]
              },
              {
                name: 'Urban Space',
                children: [
                  {
                    name: 'Discontinuous Urban Fabric',
                    value: 'Discontinuous Urban Fabric'
                  },
                  {
                    name: 'Continuous Urban Fabric',
                    value: 'Continuous Urban Fabric'
                  },
                  { name: 'Green Urban Areas', value: 'Green Urban Areas' },
                  { name: 'Vacant Lot', value: 'Vacant Lot' },
                  { name: 'Moors/Barren Land', value: 'Moors/Barren Land' },
                  { name: 'Burnt Areas', value: 'Burnt Areas' }
                ]
              }
            ]
          },
          {
            name: 'Building Area',
            children: [
              {
                name: 'Building',
                children: [
                  { name: 'High Building', value: 'High Building' },
                  { name: 'Low Building', value: 'Low Building' },
                  { name: 'Dense Tall Building', value: 'Dense Tall Building' },
                  {
                    name: 'Medium Density Scattered Building',
                    value: 'Medium Density Scattered Building'
                  },
                  {
                    name: 'Medium Density Structured Building',
                    value: 'Medium Density Structured Building'
                  },
                  { name: 'Low Scattered Building', value: 'Low Scattered Building' },
                  { name: 'City Building', value: 'City Building' },
                  { name: 'Township Building', value: 'Township Building' },
                  { name: 'Construction Site', value: 'Construction Site' },
                  { name: 'Skyscrapers', value: 'Skyscrapers' }
                ]
              },
              {
                name: 'Residential Land',
                children: [
                  { name: 'Dense Residential', value: 'Dense Residential' },
                  { name: 'Medium Residential', value: 'Medium Residential' },
                  { name: 'Sparse Residential', value: 'Sparse Residential' },
                  { name: 'Rural Residential', value: 'Rural Residential' },
                  { name: 'Urban Residential', value: 'Urban Residential' },
                  { name: 'Detached House', value: 'Detached House' },
                  { name: 'Apartment', value: 'Apartment' },
                  { name: 'Coastal Mansion', value: 'Coastal Mansion' }
                ]
              },
              {
                name: 'Infrastructure',
                children: [
                  { name: 'Store', value: 'Store' },
                  { name: 'Hospital', value: 'Hospital' },
                  { name: 'Nursing Home', value: 'Nursing Home' },
                  { name: 'School', value: 'School' },
                  { name: 'Exhibition Center', value: 'Exhibition Center' },
                  { name: 'Park', value: 'Park' },
                  { name: 'Mobile Home Park', value: 'Mobile Home Park' },
                  { name: 'Tower', value: 'Tower' },
                  { name: 'Palace', value: 'Palace' },
                  { name: 'Statue', value: 'Statue' },
                  { name: 'Resort', value: 'Resort' },
                  { name: 'Church', value: 'Church' },
                  { name: 'Cemetery', value: 'Cemetery' }
                ]
              }
            ]
          },
          {
            name: 'Manually Pile&Dig',
            children: [
              {
                name: 'Mining Land',
                children: [
                  { name: 'Conventional Mining', value: 'Conventional Mining' },
                  { name: 'Artisinal Mining', value: 'Artisinal Mining' },
                  { name: 'Quarry Area', value: 'Quarry Area' },
                  { name: 'Oil Gas Field', value: 'Oil Gas Field' },
                  {
                    name: 'Robotic Arm Transporter',
                    value: 'Robotic Arm Transporter'
                  },
                  { name: 'Oil Well', value: 'Oil Well' }
                ]
              }
            ]
          }
        ]
      },
      data_object: {
        name: "object class",
        children: [
        {
          name: "Moving Object",
          children: [
            {name: "Person",
              children: [
              {name: "Pedestrian", value: "Pedestrian"}]},
            {name: "Plane",
              children: [
              {name: "Fixed_Wing_Aircraft", value: "Fixed_Wing_Aircraft"},
                {name: "Small_Aircraft", value: "Small_Aircraft"},
                {name: "Cargo_Plane", value: "Cargo_Plane"},
                {name: "Helicopter", value: "Helicopter"}]},
            {name: "Shipping",
              children: [
                {name: "Motoboat", value: "Motoboat"},
                {name: "Yacht", value: "Yacht"},
                {name: "Sailboat", value: "Sailboat"},
                {name: "Tugboat", value: "Tugboat"},
                {name: "Barge", value: "Barge"},
                {name: "Fishing_Vessel", value: "Fishing_Vessel"},
                {name: "Ferry", value: "Ferry"},
                {name: "Container_Ship", value: "Container_Ship"},
                {name: "Oil_Tanker", value: "Oil_Tanker"},
                {name: "Engineering_Vessel", value: "Engineering_Vessel"},
                {name: "Ship", value: "Ship"},
                {name: "Multi_Ship", value: "Multi_Ship"},
                {name: "Coast_Ship", value: "Coast_Ship"},
                {name: "Detail", value: "Detail"}]},
                {name: "Train",
                  children: [
                    {name: "Locomotive", value: "Locomotive"}]},
                {name: "Vehicle",
                  children: [
                    {name: "Small_Vehicle", value: "Small_Vehicle"},
                {name: "Large_Vehicle", value: "Large_Vehicle"},
                {name: "Vehicle_Lot", value: "Vehicle_Lot"},
                {name: "Pick_Up", value: "Pick_Up"},
                {name: "Long_Truck", value: "Long_Truck"},
                {name: "Cargo_Truck", value: "Cargo_Truck"},
                {name: "Truck_W/Box", value: "Truck_W/Box"},
                {name: "Truck_W/Flatbed", value: "Truck_W/Flatbed"},
                {name: "Truck_W/Liquid", value: "Truck_W/Liquid"},
                {name: "Pickup_Truck", value: "Pickup_Truck"},
                {name: "Utility_Truck", value: "Utility_Truck"},
                {name: "Van", value: "Van"},
                {name: "Car_With_Trailer", value: "Car_With_Trailer"},
                {name: "Truck_Tractor_Trailer", value: "Truck_Tractor_Trailer"},
                {name: "Truck_With_Trailer", value: "Truck_With_Trailer"},
                {name: "Tower_Crane", value: "Tower_Crane"},
                {name: "Container_Crane", value: "Container_Crane"},
                {name: "Mobile_Crane", value: "Mobile_Crane"},
                {name: "Front_Loader", value: "Front_Loader"},
                {name: "Crane_Truck", value: "Crane_Truck"},
                {name: "Truck", value: "Truck"},
                {name: "Lorry", value: "Lorry"},
                {name: "Motor", value: "Motor"},
                {name: "Bicycle", value: "Bicycle"},
                {name: "Tricycle", value: "Tricycle"},
                {name: "Passenger_Vehicle", value: "Passenger_Vehicle"},
                {name: "Cargo_Car", value: "Cargo_Car"},
                {name: "Flat_Car", value: "Flat_Car"},
                {name: "Tank_Car", value: "Tank_Car"},
                {name: "Reach_Stacker", value: "Reach_Stacker"},
                {name: "Straddle_Carrier", value: "Straddle_Carrier"},
                {name: "Dump_Truck", value: "Dump_Truck"},
                {name: "Haul_Truck", value: "Haul_Truck"},
                {name: "Scraper", value: "Scraper"},
                {name: "Tractor", value: "Tractor"},
                {name: "Excavator", value: "Excavator"},
                {name: "Cement_Mixer", value: "Cement_Mixer"},
                {name: "Ground_Grader", value: "Ground_Grader"},
                {name: "Bus", value: "Bus"},
                {name: "Camping_Car", value: "Camping_Car"}]}]},
          {name: "Fixed Object",
            children: [
              {name: "Public_Facility",
                children: [
                {name: "Playground", value: "Playground"},
                {name: "Baseball_Diamond", value: "Baseball_Diamond"},
                {name: "Tennis_Court", value: "Tennis_Court"},
                {name: "Basketball_Court", value: "Basketball_Court"},
                {name: "Ground_Track_Field", value: "Ground_Track_Field"},
                {name: "Soccerball_Field", value: "Soccerball_Field"},
                {name: "Swimming_Pool", value: "Swimming_Pool"},
                {name: "Golf_Course", value: "Golf_Course"}]},
                {name: "Transportation",
                  children: [
                    {name: "Harbor", value: "Harbor"},
                {name: "Train_Station", value: "Train_Station"},
                {name: "Overpass", value: "Overpass"},
                {name: "Bridge", value: "Bridge"},
                {name: "Aircraft_Hangar", value: "Aircraft_Hangar"},
                {name: "Helipad", value: "Helipad"},
                {name: "Parking_Lot", value: "Parking_Lot"},
                {name: "Airport", value: "Airport"},
                {name: "Roundabout", value: "Roundabout"},
                {name: "Crossroad", value: "Crossroad"},
                {name: "T_Junction", value: "T_Junction"},
                {name: "Expressway_Service_Area", value: "Expressway_Service_Area"},
                {name: "Expressway_Toll_Station", value: "Expressway_Toll_Station"},
                {name: "Dam", value: "Dam"}]},
                {name: "Building",
                  children: [
                    {name: "Hut/Tent", value: "Hut/Tent"},
                {name: "Shed", value: "Shed"},
                {name: "Tower_Structure", value: "Tower_Structure"},
                {name: "Facility", value: "Facility"},
                {name: "Damaged_Building", value: "Damaged_Building"},
                {name: "Construction_Site", value: "Construction_Site"}]},
                {name: "Industrial",
                  children: [
                    {name: "Wind_Mill", value: "Wind_Mill"},
                {name: "Shipping_Container", value: "Shipping_Container"},
                {name: "Oiltank", value: "Oiltank"},
                {name: "Storage_Tank", value: "Storage_Tank"},
                {name: "Chimney", value: "Chimney"},
                {name: "Pylon", value: "Pylon"}]},
                {name: "Other",
                  children: [
                    {name: "Sea", value: "Sea"},
                {name: "Coast", value: "Coast"},
                {name: "Land", value: "Land"}]}]}]},
      data_landCover: {
        name: 'landCover class',
        children: [{name:'Arable Land',children: [{name: 'Farmland', children: [{name: 'Paddy Field',value: 'Paddy Field'},
              {name: 'Irrigated Land',value: 'Irrigated Land'},
              {name: 'Dry Land',value: 'Dry Land'},
              {name: 'Greenhouse',value: 'Greenhouse'}]},
            {name: 'Green Land', children: [{name: 'Orchard',value: 'Orchard'},
                {name: 'Tea Garden',value: 'Tea Garden'},
                {name: 'Rubber Plantation',value: 'Rubber Plantation'},
                {name: 'Mulberry Garden',value: 'Mulberry Garden'},
                {name: 'Nursery',value: 'Nursery'},
                {name: 'Flower Bed',value: 'Flower Bed'},
                {name: 'Other Gardens',value: 'Other Gardens'}]}]},

          {name:'Woodland&Grass',children: [{name: 'Woodland', children: [{name: 'Arbor Woodland',value: 'Arbor Woodland'},
                {name: 'Bamboo Forest Land',value: 'Bamboo Forest Land'},
                {name: 'Shrubbery',value: 'Shrubbery'},
                {name: 'Tree-Shurb Mixed ',value: 'Tree-Shurb Mixed '},
                {name: 'Green Forest Land',value: 'Green Forest Land'},
                {name: 'Open Woodland',value: 'Open Woodland'},
                {name: 'Young Plantation',value: 'Young Plantation'},
                {name: 'Other Woodlands',value: 'Other Woodlands'}]},
              {name: 'Grass', children: [{name: 'Natural Grassland',value: 'Natural Grassland'},
                  {name: 'Artificial Grassland',value: 'Artificial Grassland'},
                  {name: 'Other Grasslands',value: 'Other Grasslands'}]}]},

          {name:'Building Area',children: [{name: 'Residential Land', children: [{name: 'Urban Housing',value: 'Urban Housing'},
                {name: 'Rural Housing',value: 'Rural Housing'}]},
              {name: 'Commercial Land', children: [{name: 'Retail Commercial Land',value: 'Retail Commercial Land'},
                  {name: 'Land For Wholesale Market',value: 'Land For Wholesale Market'}]},
              {name: 'Industrial Land', children: [{name: 'Class I Industrial Land',value: 'Class I Industrial Land'},
                  {name: 'Class Ii Industrial Land',value: 'Class Ii Industrial Land'},
                  {name: 'Class Iii Industrial Land',value: 'Class Iii Industrial Land'}]}]},

          {name:'Transportation',children: [{name: 'Railway', children: []},
              {name: 'Road', children: []},
              {name: 'Field Road', children: []},
              {name: 'Airport', children: []},
              {name: 'Harbor', children: []},
              {name: 'Traffic Station', children: [{name: 'Land For External Traffic Station',value: 'Land For External Traffic Station'},
                  {name: 'Land For Public Transport Station',value: 'Land For Public Transport Station'},
                  {name: 'Land For Social Parking Lot',value: 'Land For Social Parking Lot'}]},
              {name: 'Other Transportation Facilities', children: []}]},

          {name:'Structures',children: [{name: 'Impervious Surface', children: [{name: 'Square',value: 'Square'},
                {name: 'Hardened Slope Protection',value: 'Hardened Slope Protection'}]},
              {name: 'Sports Facilities', children: [{name: 'Baseball Field',value: 'Baseball Field'},
                  {name: 'Basketball Court',value: 'Basketball Court'},
                  {name: 'Golf Course',value: 'Golf Course'},
                  {name: 'Football Field',value: 'Football Field'},
                  {name: 'Tennis Court',value: 'Tennis Court'},
                  {name: 'Playground',value: 'Playground'},
                  {name: 'Ground Track And Field',value: 'Ground Track And Field'},
                  {name: 'Sports And Leisure Facilities',value: 'Sports And Leisure Facilities'},
                  {name: 'Swimming Pool',value: 'Swimming Pool'}]},
              {name: 'Storage Facilities', children: [{name: 'Oil Tank',value: 'Oil Tank'},
                  {name: 'Storage Tank',value: 'Storage Tank'},
                  {name: 'Container',value: 'Container'},
                  {name: 'Storage Room',value: 'Storage Room'}]},
              {name: 'Public Facilities', children: [{name: 'Land For Water Supply Facilities',value: 'Land For Water Supply Facilities'},
                  {name: 'Land For Drainage Facilities',value: 'Land For Drainage Facilities'},
                  {name: 'Land For Power Supply Facilities',value: 'Land For Power Supply Facilities'},
                  {name: 'Land For Gas Facilities',value: 'Land For Gas Facilities'},
                  {name: 'Land For Heating Facilities',value: 'Land For Heating Facilities'},
                  {name: 'Land For Sanitation Facilities',value: 'Land For Sanitation Facilities'},
                  {name: 'Land For Fire Fighting Facilities',value: 'Land For Fire Fighting Facilities'},
                  {name: 'Main Canal',value: 'Main Canal'},
                  {name: 'Other Utilities',value: 'Other Utilities'}]},
              {name: 'Public Management&Service Facilities', children: [{name: 'Cultural Land',value: 'Cultural Land'},
                  {name: 'Medical Land',value: 'Medical Land'},
                  {name: 'Land For Education',value: 'Land For Education'}]}]},

          {name:'Manually Pile&Dig',children: [{name: 'Open Pit Excavation', children: [{name: 'Open Pit Coal Mine Excavation Field',value: 'Open Pit Coal Mine Excavation Field'},
                {name: 'Strip Pit',value: 'Strip Pit'},
                {name: 'Other Excavation Sites',value: 'Other Excavation Sites'}]},
              {name: 'Stacking Yard', children: [{name: 'Garbage Dump',value: 'Garbage Dump'},
                  {name: 'Other Yards',value: 'Other Yards'}]},
              {name: 'Other Artificial Piles', children: []}]},
          {name:'Desert&Bare Land',children: [{name: 'Salinity Surface', children: [{name: 'Saline Alkali Land',value: 'Saline Alkali Land'}]},
              {name: 'Sandy Surface', children: [{name: 'Desert',value: 'Desert'}]},
              {name: 'Rocky Surface', children: [{name: 'Bare Rock',value: 'Bare Rock'}]}]},
          {name:'Waters',children: [{name: 'Continental Water', children: [{name: 'River Surface',value: 'River Surface'},
                {name: 'Lake Surface',value: 'Lake Surface'},
                {name: 'Reservoir And Pond',value: 'Reservoir And Pond'},
                {name: 'Ditch',value: 'Ditch'}]},
              {name: 'Glaciers&perennial snow cover', children: []},
              {name: 'Ocean', children: []},
              {name: 'Wetland', children: [
                  {name: 'Forest Swamp',value: 'Forest Swamp'},
                  {name: 'Shrub Swamp ',value: 'Shrub Swamp '},
                  {name: 'Swamp Grassland',value: 'Swamp Grassland'},
                  {name: 'Other Marshes',value: 'Other Marshes'},
                  {name: 'Coastal Beach',value: 'Coastal Beach'},
                  {name: 'Inland Beach',value: 'Inland Beach'},
                  {name: 'Mangrove Woodland',value: 'Mangrove Woodland'}]}]},
          {name:'Other',children: [{name: 'Vacant Land', children: []},
              {name: 'Special Land', children: [{name: 'Funeral Land',value: 'Funeral Land'},
                  {name: 'Other Special Land',value: 'Other Special Land'}]}]}
        ]
      },
      option:  {
        tooltip: {
          trigger: 'item',
          triggerOn: 'mousemove'
        },
        series: [
          {
            type: 'tree',
            id: 0,
            name: 'tree1',
            data: [],
            textStyle:{
              fontSize:50
            },


            top: '2%',
            left: '8%',
            bottom: '2%',
            right: '22%',

            symbolSize: 12, //圆形大小

            edgeShape: 'polyline',
            edgeForkPosition: '63%',
            initialTreeDepth: 3,

            lineStyle: {
              width: 2
            },

            label: {
              backgroundColor: '#fff',
              position: 'left',
              verticalAlign: 'middle',
              align: 'right',
              textStyle:{
                fontSize:14 //字体大小
              },
            },

            leaves: {
              label: {
                position: 'right',
                verticalAlign: 'middle',
                align: 'left',
              },
            },

            emphasis: {
              focus: 'descendant'
            },

            expandAndCollapse: true,
            animationDuration: 550,
            animationDurationUpdate: 750
          }
        ]
      }
    }

  },
  methods: {

    drawCharts(activeKey) {

      var chartDom;
      var optionScene = this.option
      if(activeKey == 1){
        chartDom = document.getElementById('scene-main')
        optionScene.series[0].data = [this.data_scene]
      }
      if(activeKey == 2){
        chartDom = document.getElementById('object-main')
        optionScene.series[0].data = [this.data_object]
      }
      if(activeKey == 3){
        chartDom = document.getElementById('landCover-main')
        optionScene.series[0].data = [this.data_landCover]
      }
      var sceneChart = echarts.init(chartDom);
      sceneChart.setOption(optionScene);
    },
    tabChange(activeKey) {
      console.log(activeKey)
      this.$nextTick(()=>{
        this.drawCharts(activeKey)
      })

    }

  },
  mounted() {
    this.drawCharts(1)
  }
}
</script>


<style scoped>
.area {
  background-color: #FFFFFF;
}
.charts {
  width: 100%;
  height:4000px
}


</style>