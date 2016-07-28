select * from cycle;
select * from cycle_elements;
select * from element;
select * from fact;
select * from feature;
select * from feature_model;
select * from inference;
select * from project;
select * from reference;

SELECT e_from.identifier from_elem, e_to.identifier to_elem FROM reference r INNER JOIN element e_from ON r.from_id_elem = e_from.id_element INNER JOIN element e_to ON r.to_id_elem = e_to.id_element WHERE r.id_project = 7;

/*UPDATE project SET date = '2016/03/24' WHERE id_project = 7;*/

SELECT e.identifier, e.is_hook, e.is_fprivate, e.is_fpublic, e.id_feature, f.feature_name FROM element e INNER JOIN feature f ON e.id_feature = f.id_feature WHERE f.id_feature_model = 3;

SELECT f.id_fact, f.fact, e.identifier, f.elem_fact_isfprivate, fe.feature_name FROM fact f INNER JOIN element e ON f.id_element_fact = e.id_element INNER JOIN feature fe ON f.id_feature = fe.id_feature 
WHERE fe.id_feature_model = 3 ORDER BY f.id_fact;

SELECT inference FROM inference WHERE id_fact = 15 ORDER BY id_inference;

SELECT e.identifier FROM inference_element ie INNER JOIN element e ON ie.id_element = e.id_element  WHERE id_fact = 15;

SELECT id_element FROM element WHERE identifier = "opl" AND id_project = 10;


UPDATE element SET identifier = ?, type = ?, code = ?, is_hook = ?, in_cycle = ?, is_fprivate = ?, is_fpublic = ?, id_project = ?, id_feature = ? WHERE id_element = ?;


SELECT i.inference, e.identifier FROM inference i LEFT JOIN element e ON i.id_element = e.id_element WHERE id_fact = 186 ORDER BY id_inference;

	
select * from inference where id_element is null;

select * from element where identifier like 'logicGates.Gate.allInputsUsed';
select * from element where identifier like 'logicGates.Gate.allOutputsUsed';
/**
update inference set id_element = 733 where id_inference = 600;
update inference set id_element = 734 where id_inference = 610;
*/