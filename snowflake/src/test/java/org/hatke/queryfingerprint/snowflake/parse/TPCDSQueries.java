package org.hatke.queryfingerprint.snowflake.parse;

public class TPCDSQueries {

    static String q1 = "with customer_total_return as\n" +
            "(select sr_customer_sk as ctr_customer_sk\n" +
            ",sr_store_sk as ctr_store_sk\n" +
            ",sum(SR_RETURN_AMT_INC_TAX) as ctr_total_return\n" +
            "from store_returns\n" +
            ",date_dim\n" +
            "where sr_returned_date_sk = d_date_sk\n" +
            "and d_year =2001\n" +
            "group by sr_customer_sk\n" +
            ",sr_store_sk)\n" +
            " select  c_customer_id\n" +
            "from customer_total_return ctr1\n" +
            ",store\n" +
            ",customer\n" +
            "where ctr1.ctr_total_return > (select avg(ctr_total_return)*1.2\n" +
            "from customer_total_return ctr2\n" +
            "where ctr1.ctr_store_sk = ctr2.ctr_store_sk)\n" +
            "and s_store_sk = ctr1.ctr_store_sk\n" +
            "and s_state = 'distmember(fips_county, 61, 3)'\n" +
            "and ctr1.ctr_customer_sk = c_customer_sk\n" +
            "order by c_customer_id\n" +
            "LIMIT 100";

    static String q2 = " with wscs as\n" +
            " (select sold_date_sk\n" +
            "        ,sales_price\n" +
            "  from (select ws_sold_date_sk sold_date_sk\n" +
            "              ,ws_ext_sales_price sales_price\n" +
            "        from web_sales) x\n" +
            "        union all\n" +
            "       (select cs_sold_date_sk sold_date_sk\n" +
            "              ,cs_ext_sales_price sales_price\n" +
            "        from catalog_sales)),\n" +
            " wswscs as \n" +
            " (select d_week_seq,\n" +
            "        sum(case when (d_day_name='Sunday') then sales_price else null end) sun_sales,\n" +
            "        sum(case when (d_day_name='Monday') then sales_price else null end) mon_sales,\n" +
            "        sum(case when (d_day_name='Tuesday') then sales_price else  null end) tue_sales,\n" +
            "        sum(case when (d_day_name='Wednesday') then sales_price else null end) wed_sales,\n" +
            "        sum(case when (d_day_name='Thursday') then sales_price else null end) thu_sales,\n" +
            "        sum(case when (d_day_name='Friday') then sales_price else null end) fri_sales,\n" +
            "        sum(case when (d_day_name='Saturday') then sales_price else null end) sat_sales\n" +
            " from wscs\n" +
            "     ,date_dim\n" +
            " where d_date_sk = sold_date_sk\n" +
            " group by d_week_seq)\n" +
            " select d_week_seq1\n" +
            "       ,round(sun_sales1/sun_sales2,2)\n" +
            "       ,round(mon_sales1/mon_sales2,2)\n" +
            "       ,round(tue_sales1/tue_sales2,2)\n" +
            "       ,round(wed_sales1/wed_sales2,2)\n" +
            "       ,round(thu_sales1/thu_sales2,2)\n" +
            "       ,round(fri_sales1/fri_sales2,2)\n" +
            "       ,round(sat_sales1/sat_sales2,2)\n" +
            " from\n" +
            " (select wswscs.d_week_seq d_week_seq1\n" +
            "        ,sun_sales sun_sales1\n" +
            "        ,mon_sales mon_sales1\n" +
            "        ,tue_sales tue_sales1\n" +
            "        ,wed_sales wed_sales1\n" +
            "        ,thu_sales thu_sales1\n" +
            "        ,fri_sales fri_sales1\n" +
            "        ,sat_sales sat_sales1\n" +
            "  from wswscs,date_dim \n" +
            "  where date_dim.d_week_seq = wswscs.d_week_seq and\n" +
            "        d_year = 2001) y,\n" +
            " (select wswscs.d_week_seq d_week_seq2\n" +
            "        ,sun_sales sun_sales2\n" +
            "        ,mon_sales mon_sales2\n" +
            "        ,tue_sales tue_sales2\n" +
            "        ,wed_sales wed_sales2\n" +
            "        ,thu_sales thu_sales2\n" +
            "        ,fri_sales fri_sales2\n" +
            "        ,sat_sales sat_sales2\n" +
            "  from wswscs\n" +
            "      ,date_dim \n" +
            "  where date_dim.d_week_seq = wswscs.d_week_seq and\n" +
            "        d_year = 2001+1) z\n" +
            " where d_week_seq1=d_week_seq2-53\n" +
            " order by d_week_seq1";

    static String q3 = "select  dt.d_year \n" +
            "       ,item.i_brand_id brand_id \n" +
            "       ,item.i_brand brand\n" +
            "       ,sum(ss_sales_price) sum_agg\n" +
            " from  date_dim dt \n" +
            "      ,store_sales\n" +
            "      ,item\n" +
            " where dt.d_date_sk = store_sales.ss_sold_date_sk\n" +
            "   and store_sales.ss_item_sk = item.i_item_sk\n" +
            "   and item.i_manufact_id = 30\n" +
            "   and dt.d_moy=12\n" +
            " group by dt.d_year\n" +
            "      ,item.i_brand\n" +
            "      ,item.i_brand_id\n" +
            " order by dt.d_year\n" +
            "         ,sum_agg desc\n" +
            "         ,brand_id\n" +
            " LIMIT 100";

    static String q6 ="select  a.ca_state state, count(*) cnt\n" +
            "from customer_address a\n" +
            "   ,customer c\n" +
            "   ,store_sales s\n" +
            "   ,date_dim d\n" +
            "   ,item i\n" +
            "where       a.ca_address_sk = c.c_current_addr_sk\n" +
            " and c.c_customer_sk = s.ss_customer_sk\n" +
            " and s.ss_sold_date_sk = d.d_date_sk\n" +
            " and s.ss_item_sk = i.i_item_sk\n" +
            " and d.d_month_seq =\n" +
            "      (select distinct (d_month_seq)\n" +
            "       from date_dim\n" +
            "             where d_year = 2001\n" +
            "         and d_moy = 1 )\n" +
            " and i.i_current_price > 1.2 *\n" +
            "           (select avg(j.i_current_price)\n" +
            "      from item j\n" +
            "      where j.i_category = i.i_category)\n" +
            "group by a.ca_state\n" +
            "having count(*) >= 10\n" +
            "order by cnt\n" +
            "limit 100;";

    static String q9 = "select case when (select count(*)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 1 and 20) > 74129\n" +
            "          then (select avg(ss_ext_discount_amt)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 1 and 20)\n" +
            "          else (select avg(ss_net_paid)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 1 and 20) end bucket1 ,\n" +
            "     case when (select count(*)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 21 and 40) > 122840\n" +
            "          then (select avg(ss_ext_discount_amt)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 21 and 40)\n" +
            "          else (select avg(ss_net_paid)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 21 and 40) end bucket2,\n" +
            "     case when (select count(*)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 41 and 60) > 56580\n" +
            "          then (select avg(ss_ext_discount_amt)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 41 and 60)\n" +
            "          else (select avg(ss_net_paid)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 41 and 60) end bucket3,\n" +
            "     case when (select count(*)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 61 and 80) > 10097\n" +
            "          then (select avg(ss_ext_discount_amt)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 61 and 80)\n" +
            "          else (select avg(ss_net_paid)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 61 and 80) end bucket4,\n" +
            "     case when (select count(*)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 81 and 100) > 165306\n" +
            "          then (select avg(ss_ext_discount_amt)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 81 and 100)\n" +
            "          else (select avg(ss_net_paid)\n" +
            "                from store_sales\n" +
            "                where ss_quantity between 81 and 100) end bucket5\n" +
            "from reason\n" +
            "where r_reason_sk = 1";


    static String q10 = "select\n" +
            "cd_gender,\n" +
            "cd_marital_status,\n" +
            "cd_education_status,\n" +
            "count(*) cnt1,\n" +
            "cd_purchase_estimate,\n" +
            "count(*) cnt2,\n" +
            "cd_credit_rating,\n" +
            "count(*) cnt3,\n" +
            "cd_dep_count,\n" +
            "count(*) cnt4,\n" +
            "cd_dep_employed_count,\n" +
            "count(*) cnt5,\n" +
            "cd_dep_college_count,\n" +
            "count(*) cnt6\n" +
            "from\n" +
            "customer c,customer_address ca,customer_demographics\n" +
            "where\n" +
            "c.c_current_addr_sk = ca.ca_address_sk and\n" +
            "ca_county in ('Rush County','Toole County','Jefferson County','Dona Ana County','La Porte County') and\n" +
            "cd_demo_sk = c.c_current_cdemo_sk and\n" +
            "exists (select *\n" +
            "        from store_sales,date_dim\n" +
            "        where c.c_customer_sk = ss_customer_sk and\n" +
            "              ss_sold_date_sk = d_date_sk and\n" +
            "              d_year = 2002 and\n" +
            "              d_moy between 1 and 1+3) and\n" +
            " (exists (select *\n" +
            "          from web_sales,date_dim\n" +
            "          where c.c_customer_sk = ws_bill_customer_sk and\n" +
            "                ws_sold_date_sk = d_date_sk and\n" +
            "                d_year = 2002 and\n" +
            "                d_moy between 1 ANd 1+3) or\n" +
            "  exists (select *\n" +
            "          from catalog_sales,date_dim\n" +
            "          where c.c_customer_sk = cs_ship_customer_sk and\n" +
            "                cs_sold_date_sk = d_date_sk and\n" +
            "                d_year = 2002 and\n" +
            "                d_moy between 1 and 1+3))\n" +
            "group by cd_gender,\n" +
            "        cd_marital_status,\n" +
            "        cd_education_status,\n" +
            "        cd_purchase_estimate,\n" +
            "        cd_credit_rating,\n" +
            "        cd_dep_count,\n" +
            "        cd_dep_employed_count,\n" +
            "        cd_dep_college_count\n" +
            "order by cd_gender,\n" +
            "        cd_marital_status,\n" +
            "        cd_education_status,\n" +
            "        cd_purchase_estimate,\n" +
            "        cd_credit_rating,\n" +
            "        cd_dep_count,\n" +
            "        cd_dep_employed_count,\n" +
            "        cd_dep_college_count\n" +
            "limit 100";
}
