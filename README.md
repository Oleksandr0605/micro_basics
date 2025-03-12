# Lab3
## Запустити 3 екземпляри LoggingService
![img.png](images/img.png)  
В recources є три файли .properties які визначають для кожного профіля порт, тому в конфігурації логінг сервісу потрібно вказати профіль таким чином:  
![img_1.png](images/img_1.png)  
  
Hazelcast запускається за допомогою програми HazelcastCluster.  
![img_2.png](images/img_2.png)

## Записати 10 повідомлень через facade-service та прочитати їх через GET запит
![img_3.png](images/img_3.png)  

Можна бачити логи в кожному сервісі  
![img_5.png](images/img_5.png)  
![img_6.png](images/img_6.png)  
![img_7.png](images/img_7.png)  
  
Відповідно всі повідомлення отримані за допомогою GET запиту:  
![img_4.png](images/img_4.png)  
  
## Вимкнути декілька екземплярів logging-service і поглянути що буде
Після вимкнення одного екземпляру всі дані все ще зберігаються
![img_8.png](images/img_8.png)  
  
Також після відключення другого  
![img_9.png](images/img_9.png)  
  
Отримання випадкового сервісу та обробка недоступних сервісів реалізована наступним чином:  
![img_10.png](images/img_10.png)

