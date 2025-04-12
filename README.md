# Lab4

## Запустити 3 екземпляри logging-service і facade
![img.png](images/img.png)  
Щоб так запустити потрібно в конфігурації додати профілі таким чином:  
  
![img_1.png](images/img_1.png)  

## Записати 10 повідомлень (не вмикаючи message-service)
![img_3.png](images/img_3.png)  
  
І отримав таке на запит GET
![img_2.png](images/img_2.png)  
  
## Тепер включаємо message-service
![img_4.png](images/img_4.png)  
  
І даємо запит GET  
![img_5.png](images/img_5.png)  
  
Бачимо що в черзі все зберігалось і після увімкнення message сервісів все записалось.

