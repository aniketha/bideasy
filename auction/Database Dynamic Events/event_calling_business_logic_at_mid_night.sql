SET GLOBAL event_scheduler = ON;
CREATE EVENT Close_Expired_auctions
ON SCHEDULE EVERY '1' Day
STARTS '2014-04-07 00:05:00'
DO call business_logic();