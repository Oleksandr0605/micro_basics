# Lab5

#### Запустив consule
![img.png](images/img.png)

### Всі мікросервіси мають самостійно динамічно реєструватись при старті у Consul, кожного з сервісів може бути запущено кілька екземплярів (це буде відображатись як кількість instances на UI):
#### Запустив всі сервіси через spring  
![img_1.png](images/img_1.png)
#### В Consule одразу видно всі сервіси і що вони запущені
![img_2.png](images/img_2.png)

### При звертанні facade-service до logging-service та messages-service, IP-адреси (і порти) мають зчитуватись facade-service з Consul. Немає бути задано в коді чи конфігураціях статичних значень адрес.
#### Тепер береться сервіс в фасаді через Consule
![img_3.png](images/img_3.png)
#### Також і до message-service
![img_4.png](images/img_4.png)

### Налаштування для клієнтів Hazelcast мають зберігатись як key/value у Consul і зчитуватись logging-service
#### Переніс конфігурації в key/value, тепер назва мапи дл logging-service звичтується з Consule
![img_5.png](images/img_5.png)
![img_6.png](images/img_6.png)

### Налаштування для Message Queue (адреса, назва черги, …) мають зберігатись як key/value у Consul і зчитуватись facade-service та messages-service
#### Тепер всі налаштування черги зберігаються в key/value, що можна побачити на попередньому скріні і на наступному
![img_7.png](images/img_7.png)
#### Також от їх використання
![img_8.png](images/img_8.png)

### Продемонструвати, що у випадку відключення екземпляру певного мікросервісу, це буде відображатись у Consul (відключений екземпляр сервісу змінить статус) , а виклики будуть перенаправлятись до інших працюючих екземплярів.  
#### Демонстрація що все працює
![img_9.png](images/img_9.png)
![img_10.png](images/img_10.png)
![img_11.png](images/img_11.png)
#### Коли відключаю один сервіс логування він зникає з консула
![img_13.png](images/img_13.png)
![img_12.png](images/img_12.png)
#### При цьому все інше далі продовжує працювати