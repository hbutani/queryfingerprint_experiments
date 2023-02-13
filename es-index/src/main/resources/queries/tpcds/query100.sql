select *
from (
    select w_warehouse_name
        ,i_item_id
        ,sum(case when (cast(d_date as date) < cast ('1998-04-08' as date)) then inv_quantity_on_hand else 0 end) as inv_before
        ,sum(case when (cast(d_date as date) >= cast ('1998-04-08' as date)) then inv_quantity_on_hand else 0 end) as inv_after
    from inventory
        ,warehouse
        ,item
        ,date_dim
    where i_current_price between 1.20 and 1.49
        and item.i_item_sk = inventory.inv_item_sk
        and inventory.inv_warehouse_sk = warehouse.w_warehouse_sk
        and inventory.inv_date_sk = date_dim.d_date_sk
        and d_date between (cast ('1990-04-08' as date) - interval '30' days) and (cast ('1998-04-08' as date) + interval '30' days)
    group by w_warehouse_name
        , i_item_id
) x
where (case when inv_before > 0 then inv_after / inv_before else null end) between 2.0/3.0 and 3.0/2.0
order by w_warehouse_name
    ,i_item_id
limit 100;