#Control System for Quanser Tanks
This is a project developed by Jaime Dantas and Alexandre Luz which consists of an PID controller for the Quanser water tanks. Although the software was built to work with the Quanser Coupled Water Tanks, it can easily controll any kind of liquid tanks by changing the specs in the control code.

![](https://github.com/jaimedantas/Tank-Control-PID-System/blob/master/images_git/tank.png)

##Types of Control
The software TC Control is able to implements almost all types of the classical controllers. The main window shows all the input channels and it also shows the signal that is generated and sent to the pump. In addition, the operator can rely on a 2D real-time animation of the tank's levels as well as the pump flow. Several information can be shown if the operator wish to know, in details, the values of the controller.

![](https://github.com/jaimedantas/Tank-Control-PID-System/blob/master/images_git/main.png)

The user can choose to control the industrial plant with the following controllers:
* PID  Controlller
* PI-D Controller
* PI   Controller
* PD   Controller
* P    Controller

There is a window for enter the paraments of the controller. Also, the operator has the option of running the plant with a sinus, quadratic, saw, step or random wave for de the process variable. In addition, you can control several outputs (tanks) indepedently or in the master-slave configutaion.

![](https://github.com/jaimedantas/Tank-Control-PID-System/blob/master/images_git/function.png)

At the end of the day, the operator can export all the tests made in the TC Control. There is a opction for exporting graphs, tables, values and details abouts the operations of the plant in the PDF format. 

![](https://github.com/jaimedantas/Tank-Control-PID-System/blob/master/images_git/report.png)

##Offline Mode
There is also an offline mode where the user can simulate the industrial plant and execute all types of controllers he/she want to.

![](https://github.com/jaimedantas/Tank-Control-PID-System/blob/master/Raw_Data/PainelSaida2.jpg)

If you wish to know more about this project, you can read all the 6 [reports](https://github.com/jaimedantas/Tank-Control-PID-System/tree/master/Relat%C3%B3rios) written in Portuguese with more than 170 pages of pure technical information.
