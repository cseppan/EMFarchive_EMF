--widt populater needs work -- epa seems to store zeros
-- also booleanb needs to be true not t and false not f

COPY (SELECT ff.name,ff.type,ff.default_value as "default value",ff.description,ff.formatter,ff.constraints,ff.mandatory, ff.width as width,ff.spaces,ff.fix_format_start as "fixformat start",ff.fix_format_end as "fixformat end" FROM emf.fileformat_columns ff, emf.dataset_types dt WHERE dt.file_format = ff.file_format_id and dt.name = 'Flat File 2010 Point') TO '/data/tmp/20100902/flat_file_2010_point_flex_file_format.csv' WITH CSV HEADER;