create schema sms AUTHORIZATION emf;

create table sms.eecs (
  id serial primary key NOT NULL,
  eecs character varying(10) NOT NULL,
  eecs_name character varying(128) NOT NULL,
  eecs_description text,
  path ltree NOT NULL,
  UNIQUE(eecs),
  UNIQUE(eecs_name)
);

create table sms.sector_scenario (
  id serial primary key NOT NULL,
  name character varying(128) NOT NULL unique,
  description character varying(128),
  abbreviation character varying(20) NOT NULL unique,
  run_status character varying(255) NOT NULL,
  should_double_count boolean,
  annotate_inventory_with_eecs boolean,
  auto_run_qa_steps smallint,
  annotating_eecs_option smallint,
  creator int NOT NULL,
  last_modified_date timestamp without time zone NOT NULL,
  start_date timestamp without time zone,
  completion_date timestamp without time zone,
  lock_owner character varying(255),
  lock_date timestamp without time zone,
  copied_from character varying(255),
  export_directory character varying,
--  eecs_version_id integer NOT NULL REFERENCES sms.eecs_version(id),
  eecs_mappping_dataset_id integer REFERENCES emf.datasets(id),
  eecs_mappping_dataset_version integer,
  sector_mappping_dataset_id integer REFERENCES emf.datasets(id),
  sector_mappping_dataset_version integer 
  /*,
  CONSTRAINT sector_scenario_eecs_mappping_dataset_version_fkey FOREIGN KEY (eecs_mappping_dataset_version, eecs_mappping_dataset_version)
    REFERENCES emf.versions (dataset_id, version) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT sector_scenario_sector_mappping_dataset_version_fkey FOREIGN KEY (sector_mappping_dataset_id, sector_mappping_dataset_version)
    REFERENCES emf.versions (dataset_id, version) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
*/);

create table sms.sector_scenario_sector (
  id serial primary key NOT NULL,
  list_index int NOT NULL,
  sector_scenario_id int NOT NULL REFERENCES sms.sector_scenario(id),
  sector_name character varying(128) NOT NULL
);

create table sms.sector_scenario_inventory (
  id serial primary key NOT NULL,
  list_index int NOT NULL,
  sector_scenario_id int NOT NULL REFERENCES sms.sector_scenario(id),
  inventory_dataset_id integer NOT NULL REFERENCES emf.datasets(id),
  inventory_dataset_version integer NOT NULL
--  ,
  --CONSTRAINT sector_scenario_inventory_inventory_dataset_version_fkey FOREIGN KEY (inventory_dataset_id, inventory_dataset_version)
    --REFERENCES emf.versions (dataset_id, version) MATCH SIMPLE
    --ON UPDATE NO ACTION ON DELETE NO ACTION
);

create table sms.sector_scenario_output_type (
  id serial primary key NOT NULL,
  name character varying(128) NOT NULL unique
);

create table sms.sector_scenario_output (
  id serial primary key NOT NULL,
  sector_scenario_id int NOT NULL REFERENCES sms.sector_scenario(id),
  sector_scenario_output_type_id int NOT NULL REFERENCES sms.sector_scenario_output_type(id),
  output_dataset_id integer NOT NULL REFERENCES emf.datasets(id),
  inventory_dataset_id integer REFERENCES emf.datasets(id),
  inventory_dataset_version integer,
  start_date timestamp without time zone,
  completion_date timestamp without time zone,
  run_status character varying(255)
  /*,
  CONSTRAINT sector_scenario_output_inventory_dataset_version_fkey FOREIGN KEY (inventory_dataset_id, inventory_dataset_version)
    REFERENCES emf.versions (dataset_id, version) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION*/
);

INSERT INTO sms.sector_scenario_output_type (name) VALUES ('Annotated Inventory with EECS');
INSERT INTO sms.sector_scenario_output_type (name) VALUES ('Detailed Sector Mapping Result');
INSERT INTO sms.sector_scenario_output_type (name) VALUES ('Detailed EECS Mapping Result');
INSERT INTO sms.sector_scenario_output_type (name) VALUES ('Sector Specific Inventory');

INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A', 'Agriculture and Renewable Resources', 'Agriculture includes emissions from crops (fertilizer application), livestock operatins and also emissions from agriculture operations such as farm equipment, tractors, generators, etc.', 'A', 257);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A1', 'Agriculture and Renewable Resources2', 'Agriculture includes emissions from crops (fertilizer application), livestock operatins and also emissions from agriculture operations such as farm equipment, tractors, generators, etc.', 'A.1', 258);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A11', 'Crop Production', NULL, 'A.1.1', 259);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A111', 'Oilseed and Grain Farming', NULL, 'A.1.1.1', 260);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A112', 'Vegetable and Melon Farming', NULL, 'A.1.1.2', 261);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A113', 'Fruit and Tree Nut Farming', NULL, 'A.1.1.3', 262);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A114', 'Greenhouse, Nursery, and Floriculture Production', NULL, 'A.1.1.4', 263);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A119', 'Other Crop Farming', NULL, 'A.1.1.9', 264);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A12', 'Animal Production', NULL, 'A.1.2', 265);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A13', 'Forestry and Logging', NULL, 'A.1.3', 266);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A14', 'Fishing and Fisheries', NULL, 'A.1.4', 267);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A15', 'Support Activities for Agriculture', NULL, 'A.1.5', 268);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A151', 'Pesticide Application', '(e.g. cotton ginning)', 'A.1.6', 269);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A2', 'Fires', 'Includes emissions from natural (wildfires) and man-made sources (agricultural and controlled fires) involved in the management of forest and agricultural land.', 'A.2', 270);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A21', 'Wildfires', NULL, 'A.2.1', 271);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A22', 'Prescribed Burning', NULL, 'A.2.2', 272);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A23', 'Open Burning - All Types', NULL, 'A.2.2', 273);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('A4', 'Fires (Other)', NULL, 'A.4', 274);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B', 'Energy, Waste, Non-Renewable Resources', 'Includes extractive industries such as mining and oil and gas extraction.', 'B', 275);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B1', 'Natural Resources Extraction', 'Includes extractive industries such as mining and oil and gas extraction.', 'B.1', 276);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B11', 'Oil and Gas Extraction', NULL, 'B.1.1', 277);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B111', 'Crude Petroleum and Natural Gas Extraction', NULL, 'B.1.1.1', 278);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B112', 'Natural Gas Liquid Extraction', NULL, 'B.1.1.2', 279);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B12', 'Mining (except oil and gas)', NULL, 'B.1.2', 280);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B121', 'Coal Mining', NULL, 'B.1.2.1', 281);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B1211', 'Coal Mining2', 'Coal Mining', 'B.1.2.1', 282);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B122', 'Metal Ore Mining', 'Metal Ore Mining', 'B.1.2.2', 283);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B1221', 'Gold Ore Mining', NULL, 'B.1.2.2.1', 284);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B123', 'Nonmetallic Mineral Mining and Quarrying', 'Nonmetallic Mineral Mining and Quarrying', 'B.1.2.3', 285);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B1231', 'Stone Mining and Quarrying', NULL, 'B.1.2.3.1', 286);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B1232', 'Sand, Gravel, Clay, and Ceramic and Refractory Minerals Mining and Quarrying', NULL, 'B.1.2.3.2', 287);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B1233', 'Phosphate Rock Mining', NULL, 'B.1.2.3.3', 288);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B13', 'Other Extractive Processes', NULL, 'B.1.3', 289);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B2', 'Utilities (All)', 'Covers emissions from the generation of electricty from coal, nuclear, natural gas, etc.', 'B.2', 290);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B21', 'Electric Power Generation', NULL, 'B.2.1', 291);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B211', 'Hydroelectric Power Generation', NULL, 'B.2.1.1', 292);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B212', 'Fossil Fuel Power Generation', NULL, 'B.2.1.2', 293);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B2121', 'External Boilers - Electric Generation', 'External Utility Boilers', 'B.2.1.2.1', 294);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B2122', 'IC Engines - Electric Generation', 'Stationary IC Engines', 'B.2.1.2.2', 295);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B213', 'Nuclear Power Generation', NULL, 'B.2.1.3', 296);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B2131', 'Radionuclides (Nuclear Facilities)', NULL, 'B.2.1.3.1', 297);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B219', 'Other Power Generation', NULL, 'B.2.1.9', 298);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B22', 'Natural Gas Distribution', NULL, 'B.2.2', 299);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B23', 'Water, Sewage and Other Systems', NULL, 'B.2.3', 300);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B231', 'Water Supply and Irrigation', 'Water Supply and Irrigation', 'B.2.3.1', 301);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B232', 'Sewage Treatment Works', NULL, 'B.2.3.2', 302);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B3', 'Waste Management and Remediation Services', 'Includes emissions from waste disposal and treatment facilities such as landfills, POTWs, waste processing, etc.', 'B.3', 303);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B31', 'Waste Collection', NULL, 'B.3.1', 304);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B32', 'Waste Treatment and Disposal', NULL, 'B.3.2', 305);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B321', 'Solid Waste Landfills', 'Solid Waste Landfills', 'B.3.2.1', 306);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B322', 'Solid Waste Combustors and Incinerators', 'Solid Waste Combustors and Incinerators', 'B.3.2.2', 307);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B323', 'Waste to Energy', NULL, 'B.3.2.3', 308);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B324', 'Hazardous Waste Treatment and Disposal', NULL, 'B.3.2.4', 309);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B33', 'Site Remediation', NULL, 'B.3.3', 310);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B39', 'Other Waste Handling Processes', NULL, 'B.3.9', 311);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B4', 'Construction Activities', 'Includes construction related emissions as well as dust emissions from roads.', 'B.4', 312);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B41', 'Contruction of Buildings', NULL, 'B.4.1', 313);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B42', 'Construction of Roads and similar activities', NULL, 'B.4.2', 314);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B421', 'Road Construction (not including cutback asphalt)', 'Road Construction (not including cutback asphalt)', 'B.4.2.1', 315);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B422', 'Cutback Asphalt Use', 'Cutback Asphalt Use', 'B.4.2.2', 316);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('B43', 'Pipelines, Sewers, etc. (Construction Activities)', NULL, 'B.4.3', 317);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C', 'Manufacturing', 'Covers NAICS 31-33 including food products, textiles, chemicals, plastics and rubber, etc.', 'C', 318);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C1', 'Manufacturing of non-Durable Goods (food products)', 'Covers NAICS 31 including food products, textiles, chemicals, plastics and rubber, etc.', 'C.1', 319);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C11', 'Food Products manufacturing', NULL, 'C.1.2', 320);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C112', 'Vegetable Oil Manufacturing', NULL, 'C.1.1.2', 321);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C118', 'Bakeries', NULL, 'C.1.1.8', 322);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C119', 'Other Food Products Manufacturing', NULL, 'C.1.1.9', 323);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C13', 'Textile products manufacturing', NULL, 'C.1.3', 324);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C16', 'Leather goods', NULL, 'C.1.6', 325);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2', 'Manufacture of non-Durable Goods (non-food)', 'Covers NAICS 32 petroleum, chemicals, etc.', 'C.2', 326);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C21', 'Wood Products', NULL, 'C.2.1', 327);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C211', 'Sawmills and Wood Preservation', 'Sawmills and Wood Preservation', 'C.2.1.1', 328);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C212', 'Plywood and Engineered Wood Manufacturing', 'Plywood and Engineered Wood Manufacturing', 'C.2.1.2', 329);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C219', 'Other Wood Products Manufacturing', 'Other Wood Products Manufacturing', 'C.2.1.9', 330);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C22', 'Paper Products Manufacturing', NULL, 'C.2.2', 331);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C221', 'Pulp and Paper Manufacturing', 'Pulp and Paper Manufacturing', 'C.2.2.1', 332);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C222', 'Paper Products Manurfacturing2', 'Paper Products Manurfacturing', 'C.2.2.2', 333);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C23', 'Printed Products', NULL, 'C.2.3', 334);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C24', 'Petroleum Products', NULL, 'C.2.4', 335);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C241', 'Petroleum Refining', 'Petroleum Refining', 'C.2.4.1', 336);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C242', 'Asphalt Paving, Roofing, and Saturated Materials Manufacturing', 'Asphalt Paving, Roofing, and Saturated Materials Manufacturing', 'C.2.4.2', 337);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C25', 'Chemical Manufacturing', 'Includes cement', 'C.2.5', 338);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C251', 'Basic Chemical Manufacturing', 'Basic Chemical Manufacturing', 'C.2.5.1', 339);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2511', 'Organic Chemical Manufacturing', NULL, 'C.2.5.1.1', 340);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2512', 'Inorganic Chemical Manufacturing', 'Inorganic Chemical Manufacturing', 'C.2.5.1.2', 341);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2513', 'Synthetic Dye and Pigment Manufacturing', NULL, 'C.2.5.1.3', 342);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2518', 'Carbon Black Manufacturing', NULL, 'C.2.5.1.8', 343);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C252', 'Plastics, Resins, and Synthetic Fibers', 'Plastics, Resins, and Synthetic Fibers', 'C.2.5.2', 344);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2521', 'Plastic Material and Resins', NULL, 'C.2.5.2.1', 345);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2522', 'Synthetic Rubber Manufacture', NULL, 'C.2.5.2.2', 346);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2523', 'Synthetic Fiber Production', NULL, 'C.2.5.2.3', 347);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C253', 'Pesticides, Fertilizer, Ag Chemicals', 'Pesticides, Fertilizer, Ag Chemicals', 'C.2.5.3', 348);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C254', 'Pharmaceutical and Medicine Manufacturing', 'Pharmaceutical and Medicine Manufacturing', 'C.2.5.4', 349);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C255', 'Paint, Coating, and Adhesive Manufacturing', 'Paint, Coating, and Adhesive Manufacturing', 'C.2.5.5', 350);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C256', 'Cleaning Compounds and Consumer Products Manfuacturing', 'Cleaning Compounds and Consumer Products Manufacture', 'C.2.5.6', 351);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C257', 'Carbon Black Manufacturing2', 'Carbon Black Manufacturing', 'C.2.5.7', 352);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C259', 'Other Chemical Products Manufacture', 'Other Chemical Products Manufacture', 'C.2.5.9', 353);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C26', 'Plastics and Rubber Manufacturing', NULL, 'C.2.6', 354);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C261', 'Plastics Product Manufacturing', 'Plastics Product Manufacturing', 'C.2.6.1', 355);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2614', 'Polystyrene Foam Manufacturing', NULL, 'C.2.6.1.4', 356);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2615', 'Urethane and Other Foam Product (except Polystyrene) Manufacturing', NULL, 'C.2.6.1.5', 357);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C262', 'Rubber Product Manufacturing', 'Rubber Product Manufacturing', 'C.2.6.2', 358);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2621', 'Tire Manufacturing', NULL, 'C.2.6.2.1', 359);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C27', 'Nonmetallic Mineral Products Manufacturing', NULL, 'C.2.7', 360);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C271', 'Clay Product and Refractory Manufacturing', 'Clay Product and Refractory Manufacturing', 'C.2.7.1', 361);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2711', 'Pottery, Ceramics, and Plumbing Fixture Manufacturing', NULL, 'C.2.7.1.1', 362);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2712', 'Clay Building Material and Refractories Manufacturing', NULL, 'C.2.7.1.2', 363);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C272', 'Glass and Glass Product Manufacturing', 'Glass and Glass Product Manufacturing', 'C.2.7.2', 364);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2721', 'Glass Product Manufacturing (except Wet formed fiberglass matt)', NULL, 'C.2.7.2.1', 365);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2722', 'Wet Formed Fiberglass Matt', NULL, 'C.2.7.2.2', 366);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C273', 'Cement and Concrete Product Manufacturing', 'Cement and Concrete Product Manufacturing', 'C.2.7.3', 367);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2731', 'Cement Manufacturing', NULL, 'C.2.7.3.1', 368);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2732', 'Concrete Products Manufacturing', NULL, 'C.2.7.3.2', 369);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C274', 'Lime and Gypsum Product Manufacturing', 'Lime and Gypsum Product Manufacturing', 'C.2.7.4', 370);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C279', 'Other Nonmetallic Mineral Product Manufacturing', 'Other Nonmetallic Mineral Product Manufacturing', 'C.2.7.9', 371);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C2791', 'Abrasive Products Manufacturing', NULL, 'C.2.7.9.1', 372);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C28', 'Biofuels Production', NULL, 'C.2.8', 373);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3', 'Manufacturing of Durable Goods', 'Covers NAICS 33 including transportation equipment, metal products, etc', 'C.3', 374);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31', 'Primary Metal Manufacturing', NULL, 'C.3.1', 375);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C311', 'Iron and Steel Mills and Ferroalloy Manufacturing', 'Iron and Steel Mills and Ferroalloy Manufacturing', 'C.3.1.1', 376);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3111', 'Iron and Steel Mills', NULL, 'C.3.1.1.1', 377);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3112', 'Electrometallurgical Ferroalloy Product Manufacturing', NULL, 'C.3.1.1.2', 378);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3113', 'Coke Ovens', NULL, 'C.3.1.1.3', 379);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C312', 'Steel Product Manufacturing from Purchased Steel', 'Steel Product Manufacturing from Purchased Steel', 'C.3.1.2', 380);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C313', 'Alumina and Aluminum Production and Processing', 'Alumina and Aluminum Production and Processing', 'C.3.1.3', 381);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3131', 'Alumina and Aluminum Production and Processing2', 'Alumina and Aluminum Production and Processing', 'C.3.1.3', 382);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31311', 'Alumina Refining', NULL, 'C.3.1.3.1.1', 383);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31312', 'Primary Aluminum Production', NULL, 'C.3.1.3.1.2', 384);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31314', 'Secondary Smelting and Alloying of Aluminum', NULL, 'C.3.1.3.1.4', 385);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31315', 'Aluminum Sheet, Plate, and Foil Manufacturing', NULL, 'C.3.1.3.1.5', 386);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31316', 'Aluminum Extruded Product Manufacturing', NULL, 'C.3.1.3.1.6', 387);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31319', 'Other Aluminum Rolling and Drawing', NULL, 'C.3.1.3.1.9', 388);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C314', 'Nonferrous Metal (except Aluminum) Production and Processing', 'Nonferrous Metal (except Aluminum) Production and Processing', 'C.3.1.4', 389);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3141', 'Primary Smelting and Refining', NULL, 'C.3.1.4.1', 390);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C31411', 'Primary Smelting and Refining of Nonferrous Metal (except Aluminum)', NULL, 'C.3.1.4.1.1', 391);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3142', 'Copper Rolling, Drawing, Extruding, and Alloying', NULL, 'C.3.1.4.2', 392);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C3143', 'Secondary Smelting, Refining, and Alloying of Nonferrous Metal (except Aluminum)', NULL, 'C.3.1.4.3', 393);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C315', 'Foundries', 'Foundries', 'C.3.1.5', 394);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C32', 'Fabricated Metal Products Manufacturing', NULL, 'C.3.2', 395);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C321', 'Forging and Stamping', 'Forging and Stamping', 'C.3.2.1', 396);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C324', 'Boiler, Tank, and Shipping Container Manufacturing', NULL, 'C.3.2.4', 397);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C326', 'Spring and Wire Product Manufacturing', NULL, 'C.3.2.4', 398);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C328', 'Coating and Engraving of Metal (incl electroplating)', NULL, 'C.3.2.8', 399);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C33', 'Machinery Manufacturing', NULL, 'C.3.3', 400);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C34', 'Computer and Electronic Products Manufacturing', NULL, 'C.3.4', 401);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C341', 'Electrical Equipment and Component Manufacturing (non-Semiconductor)', 'Electrical Equipment and Component Manufacturing (non-Semiconductor)', 'C.3.4.1', 402);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C344', 'Semiconductor and Other Electronic Component Manufacturing', 'Semiconductor and Other Electronic Component Manufacturing', 'C.3.4.4', 403);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C346', 'Manufacturing and Reproducing Magnetic and Optical Media', 'Manufacturing and Reproducing Magnetic and Optical Media', 'C.3.4.6', 404);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C35', 'Electrical Equipment, Appliance, and Component Manufacturing', NULL, 'C.3.5', 405);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C359', 'Other Electrical Equipment (Batter Manufacturing', NULL, 'C.3.3.5.9', 406);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C36', 'Transportation Equipment Manufacturing', NULL, 'C.3.6', 407);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C361', 'Motor Vehicle Manufacturing', 'Motor Vehicle Manufacturing', 'C.3.6.1', 408);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C362', 'Aerospace Product and Parts Manufacturing', 'Aerospace Product and Parts Manufacturing', 'C.3.6.2', 409);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C365', 'Railroad Rolling Stock Manufacturing', 'Railroad Rolling Stock Manufacturing', 'C.3.6.5', 410);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C366', 'Ship and boat Building and Repair', 'Ship and boat Building and Repair', 'C.3.6.6', 411);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C367', 'Reinforced Plastic Composites Production', 'Covers many NAICS engaged in manufacture of Reinforced Plastic Products', 'C.3.6.7', 412);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C369', 'Other Transportation Equipment Manufacturing', 'Other Transportation Equipment Manufacturing', 'C.3.6.9', 413);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C37', 'Furniture and Related Products Manufacturing', NULL, 'C.3.7', 414);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C39', 'Miscellaneous Products Manufacturing', NULL, 'C.3.9', 415);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C4', 'Research and Development Activities', NULL, 'C.4', 416);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('C9', 'Miscellaneous manufacturing processes (not incl. elsewhere)', NULL, 'C.9', 417);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D', 'Services', 'Includes government, trade, commerce, professional services, etc.', 'D', 418);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D1', 'Trade', 'Includes emissions from large commercial and government facilities not involved in manufacturing such as military bases and commercial office buildings.', 'D.1', 419);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D11', 'Retail and Wholesale Trade', NULL, 'D.1.1', 420);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D111', 'Retail Trade', NULL, 'D.1.1.1', 421);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D112', 'Wholesale Trade', NULL, 'D.1.1.2', 422);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D2', 'Transportation', 'Includes emissions from transportation (on-road vehicles, off-road vehicles, aircraft, ships, rail, etc)', 'D.2', 423);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D20', 'Transport Vehicle Emissions - All', NULL, 'D.2.0', 424);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D202', 'Transport Chemicals - All', NULL, 'D.2.0.2', 425);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D204', 'Tranportation - All Types - Petroleum', NULL, 'D.2.0.4', 426);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D21', 'Air Transportation', NULL, 'D.2.1', 427);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D22', 'Rail Transportation', NULL, 'D.2.2', 428);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D221', 'Rail Transport - Vehicles', NULL, 'D.2.2.1', 429);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D222', 'Rail Transport - Support (Rail Yards)', NULL, 'D.2.2.2', 430);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D223', 'Rail Transport - Chemicals', NULL, 'D.2.2.3', 431);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D224', 'Rail Transport - Petroleum Products', NULL, 'D.2.2.4', 432);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D23', 'Marine Transport', NULL, 'D.2.3', 433);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D231', 'Marine Transport - Vehicle Emissions', NULL, 'D.2.3.1', 434);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D232', 'Marine Transport - Support Activities', NULL, 'D.2.3.2', 435);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D233', 'Marine Transport - Chemicals', NULL, 'D.2.3.3', 436);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D234', 'Marine Transport - Petroleum and Lubricants', NULL, 'D.2.3.4', 437);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D24', 'Motor Vehicles', NULL, 'D.2.4', 438);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D241', 'Highway Vehicles', NULL, 'D.2.4.1', 439);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D242', 'Off-Road Vehicles', NULL, 'D.2.4.2', 440);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D243', 'Non-Road Vehicles', NULL, 'D.2.4.3', 441);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D244', 'Truck Transport - Petroleum Products', NULL, 'D.2.4.4', 442);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D25', 'Pipelines', NULL, 'D.2.5', 443);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D254', 'Pipelines - Petroleum Transport', NULL, 'D.2.5.4', 444);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D255', 'Pipelines - Natural Gas Transport', NULL, 'D.2.5.5', 445);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D28', 'Transportation Corridor (Ports, Airports, and Transportion Facilities)', NULL, 'D.2.8', 446);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D281', 'Support Activities for Water Transport', 'Port Facilities', 'D.2.8.1', 447);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D282', 'Support Activities for Air Transport', 'Airports', 'D.2.8.2', 448);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D283', 'Support Activities for Rail Transport', NULL, 'D.2.8.3', 449);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D284', 'Support Activities for Road Transportation', NULL, 'D.2.8.4', 450);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D289', 'OtherTransportation Support Activities', NULL, 'D.2.8.9', 451);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D29', 'Warehousing and Storage', NULL, 'D.2.9', 452);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D293', 'Grain Elevators (Farm Product Warehousing and Storage)', NULL, 'D.2.9.3', 453);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D299', 'Other Warehousing and Storage', NULL, 'D.2.9.9', 454);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D3', 'Health Care', NULL, 'D.3', 455);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D4', 'Services2', NULL, 'D.4', 456);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D41', 'Professional Services', NULL, 'D.4.1', 457);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D42', 'Educational Services', NULL, 'D.4.2', 458);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D43', 'Arts, Entertainment, and Recreation', NULL, 'D.4.3', 459);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D44', 'Accomadation and Food Services', NULL, 'D.4.4', 460);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D5', 'Organic Liquids Distribution', 'Includes emissions from Pipelines', 'D.5', 461);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D51', 'Organic Liquids Distribution and Marketing', NULL, 'D.5.1', 462);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D511', 'Petroleum Bulk Storage', NULL, 'D.5.1.1', 463);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D512', 'Petroleum Storage Other', NULL, 'D.5.1.2', 464);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D52', 'Organic Liquids Storage (not incl in other sectors)', NULL, 'D.5.2', 465);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D521', 'Organic Chemicals Bulk Storage', NULL, 'D.5.2.1', 466);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D53', 'Gasoline Marketing and Distributiion', NULL, 'D.5.3', 467);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D6', 'Service Industries', 'Includes dry cleaners and post manufacturing industries', 'D.6', 468);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D61', 'Repair and Maintenance Activities', NULL, 'D.6.1', 469);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D611', 'Automotive Repair', NULL, 'D.6.1.1', 470);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D612', 'Automotive Body, Paint, Interior, and Glass Repair', NULL, 'D.6.1.2', 471);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D619', 'Other Automotive Repair Activities', NULL, 'D.6.1.9', 472);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D62', 'Personal and Laundry Services', NULL, 'D.6.2', 473);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D621', 'Drycleaning and Laundry Services', 'Drycleaning and Laundry Services', 'D.6.2.1', 474);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D63', 'Publishing and Printing', NULL, 'D.6.3', 475);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D69', 'Other Service Industries', NULL, 'D.6.9', 476);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D7', 'Public Admistration', NULL, 'D.7', 477);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D71', 'Federal Facilities', 'Incudes military and national security facilities', 'D.7.1', 478);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D711', 'Department of Defense Facilties', NULL, 'D.7.1.1', 479);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D7111', 'Department of Defense Facilities2', NULL, 'D.7.1.1.1', 480);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D7112', 'Ordanance Detonation', 'Ordanance Detonation', 'D.7.1.1.2', 481);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D8', 'Residential', 'Include emissions from residences such as residential wood burning stoves.', 'D.8', 482);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D81', 'Residential combustion sources', NULL, 'D.8.1', 483);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D82', 'Small Equipment', 'Generators, lawn mowers', 'D.8.2', 484);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D83', 'Household Product Use', 'Household Fertilizers and Chemicals', 'D.8.3', 485);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D84', 'Residential - Petroleum Products', NULL, 'D.8.4', 486);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D89', 'Other Residential Sources', NULL, 'D.8.9', 487);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('D9', 'All Other Personal Services', NULL, 'D.9', 488);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E', 'Process Based Emissions', 'Includes emissions not classified elsewhere in the EECS system.', 'E', 489);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E1', 'Emission Operations (not incl in other sectors)', 'Includes emissions not classified elsewhere in the EECS system.', 'E.1', 490);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E11', 'Commercial Sterilization', NULL, 'E.1.1', 491);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E13', 'DOE and non-EGU nuclear facilities', NULL, 'E.1.3', 492);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E14', 'Miscellaneous Emission Sources', NULL, 'E.1.4', 493);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E15', 'Surface Coating Processes', NULL, 'E.1.5', 494);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E16', 'Solvent Usage (not incl in other sectors)', NULL, 'E.1.6', 495);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E161', 'Degreasing', NULL, 'E.1.6.1', 496);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E162', 'Paint Stripping', NULL, 'E.1.6.2', 497);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E163', 'Surface Coating Operations', NULL, 'E.1.6.3', 498);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E164', 'Architectural Coatings', NULL, 'E.1.6.4', 499);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E17', 'Storage (not incl. in other sectors)', NULL, 'E.1.7', 500);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E171', 'Chemical Storage (not incl. in other sectors)', NULL, 'E.1.7.1', 501);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E172', 'Petroleum Products Storage (not inc. in other sectors)', NULL, 'E.1.7.2', 502);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E19', 'Other Miscellaneous Processes', NULL, 'E.1.9', 503);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E2', 'Combustion Sources', NULL, 'E.2', 504);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E21', 'External Combustion Boilers (not incl. in other sectors)', 'Includes emissions from Boilers and IC Engines not included under other sectors', 'E.2.1', 505);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E211', 'External Boilers - Commercial/Institutional', NULL, 'E.2.1.1', 506);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E213', 'External Boilers - Industrial', NULL, 'E.2.1.3', 507);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E214', 'Space Heaters', NULL, 'E.2.1.4', 508);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E215', 'Process Heaters', NULL, 'E.2.1.5', 509);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E22', 'Internal Combusion Sources', NULL, 'E.2.2', 510);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E221', 'IC Engines - Gas Turbine', 'Stationary Gas Turbines', 'E.2.2.1', 511);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E222', 'IC Engines - Reciprocating', NULL, 'E.2.2.2', 512);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E229', 'IC Engines - Other', NULL, 'E.2.2.9', 513);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E23', 'Engine Test Cells', 'Engine Test Cells', 'E.2.3', 514);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E3', 'End Use of Products', 'Includes emission from', 'E.3', 515);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E4', 'Roads', NULL, 'E.4', 516);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E41', 'Paved Roads', NULL, 'E.4.1', 517);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E42', 'Unpaved Roads', NULL, 'E.4.2', 518);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E719', 'Other Storage Activities', NULL, 'E.7.1.9', 519);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('E9', 'All Other Miscellaneous Sources (not elsewhere classifed)', NULL, 'E.9', 520);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F', 'Biogenic Emission Sources', 'Includes non-man made emission sources', 'F', 521);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F1', 'Agricultural Land', NULL, 'F.1', 522);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F11', 'Biogenic - Agricultural Land/Cropland and Pasture', NULL, 'F.1.1', 523);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F12', 'Biogenic - Agricultural Land/Confined Feeding Operations', NULL, 'F.1.2', 524);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F13', 'Biogenic - Agricultural Land/Orchards, Groves, Vineyards, Nurseries', NULL, 'F.1.3', 525);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F14', 'Biogenic - Forest Land', NULL, 'F.1.3', 526);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F19', 'Biogenic - Agricultural Land/Other Agricultural Land', NULL, 'F.1.9', 527);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F2', 'Non-Agricultural Land', NULL, 'F.2', 528);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F21', 'Biogenic - Barren Land', NULL, 'F.2.1', 529);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F22', 'Biogenic - Forest Land2', NULL, 'F.2.2', 530);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F23', 'Biogenic - Rangeland', NULL, 'F.2.3', 531);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F24', 'Biogenic - Soil and Water Land Use', NULL, 'F.2.4', 532);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F25', 'Biogenic - Tundra', NULL, 'F.2.5', 533);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F26', 'Biogenic - Vegetation', NULL, 'F.2.6', 534);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F29', 'Biogenic - Unknown Land Use', NULL, 'F.2.9', 535);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F3', 'Biogenic - Urban or Built-Up Land', NULL, 'F.3', 536);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F4', 'Biogenic - Water', NULL, 'F.4', 537);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F41', 'Biogenic - Wetlands', NULL, 'F.4.1', 538);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F42', 'Biogenic - Perennial Snow and Ice', NULL, 'F.4.2', 539);
INSERT INTO sms.eecs (eecs, eecs_name, eecs_description, path, id) VALUES ('F9', 'Other Natural Processes', NULL, 'F.9', 540);

-- emf.file_formats 
INSERT INTO emf.file_formats (name, description, delimiter, fixed_format, date_added, last_modified_date, creator) select 'Sector Mapping', '', '          ,          ', false, '2010-05-03 01:10:40.653', '2010-05-03 01:10:40.653', (select id from emf.users where username = 'admin') as creator;
INSERT INTO emf.file_formats (name, description, delimiter, fixed_format, date_added, last_modified_date, creator) select 'EECS Mapping', '', '          ,          ', false, '2010-05-03 01:18:16.723', '2010-05-03 01:18:16.723', (select id from emf.users where username = 'admin') as creator;
INSERT INTO emf.file_formats (name, description, delimiter, fixed_format, date_added, last_modified_date, creator) select 'EECS Detailed Mapping Result', '', '          ,          ', false, '2010-05-05 01:53:24.666', '2010-05-05 01:53:24.666', (select id from emf.users where username = 'admin') as creator;
INSERT INTO emf.file_formats (name, description, delimiter, fixed_format, date_added, last_modified_date, creator) select 'Sector Detailed Mapping Result', '', '          ,          ', false, '2010-05-05 15:26:18.468', '2010-05-05 15:26:18.468', (select id from emf.users where username = 'admin') as creator;
INSERT INTO emf.file_formats (name, description, delimiter, fixed_format, date_added, last_modified_date, creator) select 'ORL Point NATA', '', '          ,          ', false, '2010-05-06 00:41:24.708', '2010-05-06 00:41:24.708', (select id from emf.users where username = 'admin') as creator;

-- emf.dataset_types 
INSERT INTO emf.dataset_types (name, description, min_files, max_files, external, default_sortorder, importer_classname, exporter_classname, lock_owner, lock_date, table_per_dataset, creation_date, last_mod_date, creator, file_format) select 'EECS Detailed Mapping Result', '', 1, 1, false, '', 'gov.epa.emissions.commons.io.orl.FlexibleDBImporter', 'gov.epa.emissions.commons.io.orl.FlexibleDBExporter', NULL, NULL, 1, '2010-05-05 01:53:24.666', '2010-05-05 01:53:24.666', (select id from emf.users where username = 'admin') as creator, (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id;
INSERT INTO emf.dataset_types (name, description, min_files, max_files, external, default_sortorder, importer_classname, exporter_classname, lock_owner, lock_date, table_per_dataset, creation_date, last_mod_date, creator, file_format) select 'EECS Mapping', '', 1, 1, false, '', 'gov.epa.emissions.commons.io.orl.FlexibleDBImporter', 'gov.epa.emissions.commons.io.orl.FlexibleDBExporter', NULL, NULL, 1, '2010-05-03 01:18:16.723', '2010-05-03 01:18:16.723', (select id from emf.users where username = 'admin') as creator, (select id from emf.file_formats where name = 'EECS Mapping') as file_format_id;
INSERT INTO emf.dataset_types (name, description, min_files, max_files, external, default_sortorder, importer_classname, exporter_classname, lock_owner, lock_date, table_per_dataset, creation_date, last_mod_date, creator, file_format) select 'Sector Mapping', '', 1, 1, false, '', 'gov.epa.emissions.commons.io.orl.FlexibleDBImporter', 'gov.epa.emissions.commons.io.orl.FlexibleDBExporter', NULL, NULL, 1, '2010-05-03 01:10:40.653', '2010-05-03 01:10:40.653', (select id from emf.users where username = 'admin') as creator, (select id from emf.file_formats where name = 'Sector Mapping') as file_format_id;
INSERT INTO emf.dataset_types (name, description, min_files, max_files, external, default_sortorder, importer_classname, exporter_classname, lock_owner, lock_date, table_per_dataset, creation_date, last_mod_date, creator, file_format) select 'ORL Point NATA', '', 1, 1, false, '', 'gov.epa.emissions.commons.io.orl.FlexibleDBImporter', 'gov.epa.emissions.commons.io.orl.FlexibleDBExporter', NULL, NULL, 1, '2010-05-06 00:41:24.708', '2010-05-18 14:58:01.363', (select id from emf.users where username = 'admin') as creator, (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id;
INSERT INTO emf.dataset_types (name, description, min_files, max_files, external, default_sortorder, importer_classname, exporter_classname, lock_owner, lock_date, table_per_dataset, creation_date, last_mod_date, creator, file_format) select 'Sector Detailed Mapping Result', '', 1, 1, false, '', 'gov.epa.emissions.commons.io.orl.FlexibleDBImporter', 'gov.epa.emissions.commons.io.orl.FlexibleDBExporter', NULL, NULL, 1, '2010-05-05 15:26:18.468', '2010-05-05 15:27:20.208', (select id from emf.users where username = 'admin') as creator, (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id;

-- emf.fileformat_columns 
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Mapping') as file_format_id, 0, 'SECTOR', 'VARCHAR(64)', '', 'StringFormatter', '', 'NOT NULL', false, 64, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Mapping') as file_format_id, 1, 'EECS', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Mapping') as file_format_id, 2, 'MACT', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Mapping') as file_format_id, 3, 'NAICS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Mapping') as file_format_id, 4, 'SCC', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Mapping') as file_format_id, 5, 'WEIGHT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Mapping') as file_format_id, 0, 'EECS', 'VARCHAR(10)', '', 'StringFormatter', '', 'NOT NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Mapping') as file_format_id, 1, 'MACT', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Mapping') as file_format_id, 2, 'NAICS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Mapping') as file_format_id, 3, 'SCC', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Mapping') as file_format_id, 4, 'WEIGHT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 16, 'WEIGHT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 15, 'EECS', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 14, 'MAP_SCC', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 13, 'MAP_NAICS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 12, 'MAP_MACT', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 11, 'NAICS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 10, 'MACT', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 9, 'AVD_EMIS', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 8, 'ANN_EMIS', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 7, 'POLL', 'VARCHAR(16)', '', 'StringFormatter', '', 'NOT NULL', false, 16, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 6, 'PLANT', 'VARCHAR(40)', '', 'StringFormatter', '', 'NOT NULL', false, 40, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 5, 'SCC', 'VARCHAR(10)', '', 'StringFormatter', '', 'NOT NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 4, 'SEGMENT', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 3, 'STACKID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 2, 'POINTID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 1, 'PLANTID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'EECS Detailed Mapping Result') as file_format_id, 0, 'FIPS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NOT NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 7, 'POLL', 'VARCHAR(16)', '', 'StringFormatter', '', 'NOT NULL', false, 16, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 6, 'PLANT', 'VARCHAR(40)', '', 'StringFormatter', '', 'NOT NULL', false, 40, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 5, 'SCC', 'VARCHAR(10)', '', 'StringFormatter', '', 'NOT NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 10, 'EECS', 'VARCHAR(10)', '', 'StringFormatter', '', 'NOT NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 11, 'SECTOR', 'VARCHAR(64)', '', 'StringFormatter', '', 'NULL', false, 64, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 12, 'WEIGHT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 4, 'SEGMENT', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 3, 'STACKID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 2, 'POINTID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 1, 'PLANTID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NOT NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 0, 'FIPS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NOT NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 8, 'ANN_EMIS', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'Sector Detailed Mapping Result') as file_format_id, 9, 'AVD_EMIS', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 0, 'FIPS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 1, 'PLANTID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 2, 'POINTID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 3, 'STACKID', 'VARCHAR(15)', '', 'StringFormatter', '', 'NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 4, 'SEGMENT', 'VARCHAR(15)', '', 'StringFormatter', '', 'NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 5, 'PLANT', 'VARCHAR(40)', '', 'StringFormatter', '', 'NULL', false, 40, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 6, 'SCC', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 7, 'ERPTYPE', 'VARCHAR(2)', '', 'StringFormatter', '', 'NULL', false, 2, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 8, 'SRCTYPE', 'VARCHAR(2)', '', 'StringFormatter', '', 'NULL', false, 2, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 9, 'STKHGT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 10, 'STKDIAM', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 11, 'STKTEMP', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 12, 'STKFLOW', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 13, 'STKVEL', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 14, 'SIC', 'VARCHAR(4)', '', 'StringFormatter', '', 'NULL', false, 4, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 15, 'MACT', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 16, 'NAICS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 17, 'CTYPE', 'VARCHAR(1)', '', 'StringFormatter', '', 'NULL', false, 1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 18, 'XLOC', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 19, 'YLOC', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 20, 'UTMZ', 'INT2', '', 'SmallIntegerFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 21, 'POLL', 'VARCHAR(16)', '', 'StringFormatter', '', 'NULL', false, 16, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 22, 'ANN_EMIS', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 23, 'AVD_EMIS', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 24, 'CEFF', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 25, 'REFF', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 26, 'CPRI', 'INTEGER', '', 'IntegerFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 27, 'CSEC', 'INTEGER', '', 'IntegerFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 28, 'NEI_UNIQUE_ID', 'VARCHAR(20)', '', 'StringFormatter', '', 'NULL', false, 20, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 29, 'ORIS_FACILITY_CODE', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 30, 'ORIS_BOILER_ID', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 31, 'IPM_YN', 'VARCHAR(1)', '', 'CharFormatter', '', 'NULL', false, 1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 32, 'DATA_SOURCE', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 33, 'STACK_DEFAULT_FLAG', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 34, 'LOCATION_DEFAULT_FLAG', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 35, 'YEAR', 'VARCHAR(4)', '', 'StringFormatter', '', 'NULL', false, 4, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 36, 'TRIBAL_CODE', 'VARCHAR(3)', '', 'StringFormatter', '', 'NULL', false, 3, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 37, 'HORIZONTAL_AREA_FUGITIVE', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 38, 'RELEASE_HEIGHT_FUGITIVE', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 39, 'ZIPCODE', 'VARCHAR(14)', '', 'StringFormatter', '', 'NULL', false, 14, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 40, 'NAICS_FLAG', 'VARCHAR(3)', '', 'StringFormatter', '', 'NULL', false, 3, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 41, 'SIC_FLAG', 'VARCHAR(3)', '', 'StringFormatter', '', 'NULL', false, 3, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 42, 'MACT_FLAG', 'VARCHAR(15)', '', 'StringFormatter', '', 'NULL', false, 15, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 43, 'PROCESS_MACT_COMPLIANCE_STATUS', 'VARCHAR(6)', '', 'StringFormatter', '', 'NULL', false, 6, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 44, 'IPM_FACILITY', 'VARCHAR(3)', '', 'StringFormatter', '', 'NULL', false, 3, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 45, 'IPM_UNIT', 'VARCHAR(3)', '', 'StringFormatter', '', 'NULL', false, 3, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 46, 'BART_SOURCE', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 47, 'BART_UNIT', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 48, 'CONTROL_STATUS', 'VARCHAR(12)', '', 'StringFormatter', '', 'NULL', false, 12, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 49, 'START_DATE', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 50, 'END_DATE', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 51, 'WINTER_THROUGHPUT_PCT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 52, 'SPRING_THROUGHPUT_PCT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 53, 'SUMMER_THROUGHPUT_PCT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 54, 'FALL_THROUGHPUT_PCT', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 55, 'ANNUAL_AVG_DAYS_PER_WEEK', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 56, 'ANNUAL_AVG_WEEKS_PER_YEAR', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 57, 'ANNUAL_AVG_HOURS_PER_DAY', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 58, 'ANNUAL_AVG_HOURS_PER_YEAR', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 59, 'PERIOD_DAYS_PER_WEEK', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 60, 'PERIOD_WEEKS_PER_PERIOD', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 61, 'PERIOD_HOURS_PER_DAY', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 62, 'PERIOD_HOURS_PER_PERIOD', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 63, 'DESIGN_CAPACITY', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 64, 'DESIGN_CAPACITY_UNIT_NUMERATOR', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 65, 'DESIGN_CAPACITY_UNIT_DENOMINATOR', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 66, 'CONTROL_MEASURES', 'TEXT', '', 'NullFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 67, 'PCT_REDUCTION', 'TEXT', '', 'NullFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 68, 'CURRENT_COST', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 69, 'CUMULATIVE_COST', 'double precision', '', 'RealFormatter', '', 'NULL', false, -1, 0, 0, 0;
INSERT INTO emf.fileformat_columns (file_format_id, list_index, name, type, default_value, description, formatter, constraints, mandatory, width, spaces, fix_format_start, fix_format_end) select (select id from emf.file_formats where name = 'ORL Point NATA') as file_format_id, 70, 'EECS', 'VARCHAR(10)', '', 'StringFormatter', '', 'NULL', false, 10, 0, 0, 0;

insert into emf.dataset_types_keywords (dataset_type_id, list_index, keyword_id, "value", kwname)
select (select id from emf.dataset_types where "name" = 'EECS Mapping') as dataset_type_id,
  (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_keywords where dataset_type_id = (select id from emf.dataset_types where "name" = 'EECS Mapping')) as list_index,
  (select id from emf.keywords where "name" = 'INDICES') as keyword_id,
  'mact|naics|scc|weight|eecs' as "value", 'INDICES' as kwname;
  
insert into emf.dataset_types_keywords (dataset_type_id, list_index, keyword_id, "value", kwname)
select (select id from emf.dataset_types where "name" = 'Sector Mapping') as dataset_type_id,
  (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_keywords where dataset_type_id = (select id from emf.dataset_types where "name" = 'Sector Mapping')) as list_index,
  (select id from emf.keywords where "name" = 'INDICES') as keyword_id,
  'mact|naics|scc|weight|eecs|sector' as "value", 'INDICES' as kwname;
  
insert into emf.dataset_types_keywords (dataset_type_id, list_index, keyword_id, "value", kwname)
select (select id from emf.dataset_types where "name" = 'ORL Point NATA') as dataset_type_id,
  (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_keywords where dataset_type_id = (select id from emf.dataset_types where "name" = 'ORL Point NATA')) as list_index,
  (select id from emf.keywords where "name" = 'EXPORT_COLUMN_LABEL') as keyword_id,
  'false' as "value", 'EXPORT_COLUMN_LABEL' as kwname;
  
-- qa steps templates
-- Sector Detailed Mapping Result - Summarize by State and Pollutant
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by State and Pollutant', 1, 
'select 
	fips.fipsst, 
	fips.state_name, 
	sector, 
	poll, 
	sum(ann_emis)
from $TABLE[1] q
left outer join reference.fips 
on fips.state_county_fips = q.fips
and fips.country_num = ''0''
group by 
	fips.fipsst, 
	fips.state_name, 
	sector, 
	poll
order by 
	fips.fipsst, 
	fips.state_name, 
	sector, 
	poll', false, 1, ''
from emf.dataset_types dt
where name in ('Sector Detailed Mapping Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by State and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select 
	fips.fipsst, 
	fips.state_name, 
	sector, 
	poll, 
	sum(ann_emis)
from $TABLE[1] q
left outer join reference.fips 
on fips.state_county_fips = q.fips
and fips.country_num = ''0''
group by 
	fips.fipsst, 
	fips.state_name, 
	sector, 
	poll
order by 
	fips.fipsst, 
	fips.state_name, 
	sector, 
	poll'
where name = 'Summarize by State and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Sector Detailed Mapping Result'));

-- Sector Detailed Mapping Result - Multi Sector Sources
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Multi Sector Sources', 1, 
'select fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs,
	count(distinct sector) as sector_match_count
from $TABLE[1] q
group by fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs
having count(distinct sector) > 1 
order by 
	fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant', false, 1, ''
from emf.dataset_types dt
where name in ('Sector Detailed Mapping Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Multi Sector Sources');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs,
	count(distinct sector) as sector_match_count
from $TABLE[1] q
group by fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs
having count(distinct sector) > 1 
order by 
	fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant'
where name = 'Multi Sector Sources'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Sector Detailed Mapping Result'));

-- Sector Detailed Mapping Result - Sources Missing Sector
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Sources Missing Sector', 1, 
'select fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs
from $TABLE[1] q
group by fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs
having count(distinct sector) = 0  
order by 
	fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant', false, 1, ''
from emf.dataset_types dt
where name in ('Sector Detailed Mapping Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Sources Missing Sector');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs
from $TABLE[1] q
group by fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant, 
	eecs
having count(distinct sector) = 0 
order by 
	fips, 
	plantid, pointid, 
	stackid, segment, 
	scc, plant'
where name = 'Sources Missing Sector'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Sector Detailed Mapping Result'));

