\WITH all_sales AS (
 SELECT d_year
       ,i_brand_id
       ,i_class_id
       ,i_category_id
       ,i_manufact_id
       ,SUM(sales_cnt) AS sales_cnt
       ,SUM(sales_amt) AS sales_amt
 FROM (SELECT d_year
             ,i_brand_id
             ,i_class_id
             ,i_category_id
             ,i_manufact_id
             ,cs_quantity - COALESCE(cr_return_quantity,0) AS sales_cnt
             ,cs_ext_sales_price - COALESCE(cr_return_amount,0.0) AS sales_amt
       FROM catalog_sales JOIN item ON i_item_sk=cs_item_sk
                          JOIN date_dim ON d_date_sk=cs_sold_date_sk
                          LEFT JOIN catalog_returns ON (cs_order_number=cr_order_number
                                                    AND cs_item_sk=cr_item_sk)
       WHERE i_category='Sports'
       UNION ALL
       SELECT dd.d_year
             ,i.i_brand_id
             ,i.i_class_id
             ,i.i_category_id
             ,i.i_manufact_id
             ,ss.ss_quantity - COALESCE(sr.sr_return_quantity,0) AS sales_cnt
             ,ss.ss_ext_sales_price - COALESCE(sr.sr_return_amt,0.0) AS sales_amt
       FROM store_sales ss JOIN item i ON i.i_item_sk=ss.ss_item_sk
                        JOIN date_dim dd ON dd.d_date_sk=ss.ss_sold_date_sk
                        LEFT JOIN blah b ON ss.ss_ticket_number=b.number
                        LEFT JOIN store_returns sr ON (ss.ss_ticket_number=sr.sr_ticket_number
                                                AND ss.ss_item_sk=sr.sr_item_sk)
       WHERE i.i_category='Sports' -- and b.u='g'
       UNION ALL
       SELECT d_year
             ,i_brand_id
             ,i_class_id
             ,i_category_id
             ,i_manufact_id
             ,ws_quantity - COALESCE(wr_return_quantity,0) AS sales_cnt
             ,ws_ext_sales_price - COALESCE(wr_return_amt,0.0) AS sales_amt
       FROM web_sales JOIN item ON i_item_sk=ws_item_sk
                      JOIN date_dim ON d_date_sk=ws_sold_date_sk
                      LEFT JOIN web_returns ON (ws_order_number=wr_order_number
                                            AND ws_item_sk=wr_item_sk)
       WHERE i_category='Sports') sales_detail
 GROUP BY d_year, i_brand_id, i_class_id, i_category_id, i_manufact_id)
SELECT  prev_yr.d_year AS prev_year
     ,curr_yr.d_year AS year
     ,curr_yr.i_brand_id
     ,curr_yr.i_class_id
     ,curr_yr.i_category_id
     ,curr_yr.i_manufact_id
     ,prev_yr.sales_cnt AS prev_yr_cnt
     ,curr_yr.sales_cnt AS curr_yr_cnt
     ,curr_yr.sales_cnt-prev_yr.sales_cnt AS sales_cnt_diff
     ,curr_yr.sales_amt-prev_yr.sales_amt AS sales_amt_diff
FROM all_sales curr_yr, all_sales prev_yr
WHERE curr_yr.i_brand_id=prev_yr.i_brand_id
  AND curr_yr.i_class_id=prev_yr.i_class_id
  AND curr_yr.i_category_id=prev_yr.i_category_id
  AND curr_yr.i_manufact_id=prev_yr.i_manufact_id
  AND curr_yr.d_year=2002
  AND prev_yr.d_year=2002-1
  AND CAST(curr_yr.sales_cnt AS DECIMAL(17,2))/CAST(prev_yr.sales_cnt AS DECIMAL(17,2))<0.9
ORDER BY sales_cnt_diff
limit 100;