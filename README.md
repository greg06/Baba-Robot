﻿Baba-Robot
==========

## Présentation
Baba est un robot combinant le meilleur de la carte raspberry et arduino.
L'objectif est d'arriver à produire un robot explorateur ludique.

![Photo de Baba](https://lh6.googleusercontent.com/-d4xxWBYGvpk/UJrNrjxpw5I/AAAAAAAAHHg/UT7qXzCHGqg/s640/20121107_220702.jpg)

##Import du projet sur raspberry
``` bash
   #installation de git si besoin.
   [[ -z $(which git-core) ]] && sudo apt-get install git-core build-essential
   cd ~
   git clone https://github.com/greg06/Baba-Robot.git
```

Si vous avez déjà importé le projet et souhaitez le mettre à jour
``` bash
   cd ~/Baba-Robot/ && git pull
```

*  [Optimisation Raspberry](https://github.com/greg06/Baba-Robot/tree/master/doc/software/raspberry)
*  [Installation de node et récupération des packages](https://github.com/greg06/Baba-Robot/tree/master/doc/software/node)
*  [Configuration du wifi](https://github.com/greg06/Baba-Robot/tree/master/doc/software/wifi)
*  [Configuration de la webcam](https://github.com/greg06/Baba-Robot/tree/master/doc/software/webcam)
*  [Configuration du son](https://github.com/greg06/Baba-Robot/tree/master/doc/software/sound)

## Caractéristique technique
*  4 roues motrices avec Moteur 6v et roue codeuse sur les roues arrières
*  Tourelle 2 axes
*  2 capteurs de distance à US (3cm à 6m)
*  1 capteur de temperature
*  Eclairage par led
*  Wifi
*  Webcam
*  Haut parleur avec ampli 2 watts
*  Carte Raspberry Pi
*  Alimentation 5 x 1,2v 2550mAh

## Galerie photos
Quelques photos sont disponibles sur picasa web [C'est ici](https://picasaweb.google.com/gregoryfromain/Baba?authuser=0&feat=directlink)

## Licence
[![Contrat Creative Commons](http://i.creativecommons.org/l/by-nc/3.0/88x31.png)](http://creativecommons.org/licenses/by-nc/3.0/)

**Baba-Robot** de **Grégory Fromain** est mis à disposition selon les termes de la [licence Creative Commons Paternité - Pas d'Utilisation Commerciale 3.0 non transcrit](http://creativecommons.org/licenses/by-nc/3.0/).
Basé(e) sur une oeuvre à [github.com](https://github.com/greg06/Baba-Robot).