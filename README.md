# TubiPlayer
The official android player of [Tubi TV](https://www.tubi.tv) -- A free movie and TV streaming service.

# Purposes of TubiPlayer
In the industry of streaming free multi-media contents to users, companies choose to show in-stream video ads to users between the main content as their business model. Examples include **YouTube** and **Tubi TV**.

From the technology side of things, in order to implement an android player that streams multiple media very well, switches between movies and ads dynamically, and presents users with a seamless user experience, it requires a series of stateful actions that poses a lot of complexities for enginners to manage. **TubiPlayer offers an industry standard, multi-media streaming and ad-supported solution on the Android platform, with highly customized code architecture and implementation**

# What Does TubiPlayer Do Well.
* Native multi-media streaming on Android
* Industry standard Ad solutions, including VAST and VPAID
* Error-handing
* Seamless UX 
* Highly customized module


# State Machine Diagram
![State Machine](https://github.com/Tubitv/TubiPlayer/blob/master/lib/doc/Screen%20Shot%202017-09-18%20at%204.23.53%20PM.png)

# How to use 
Different application often requires different sets of rules and logic to satisfy business requirements, as the result, Tubiplayer was build to provide high degree of
customization. However, highly customized code if not managed well can quickly turn into nightmare. Therefore, Tubiplayer leverages a third party Dependency Injection framework:[Dagger](https://github.com/google/dagger) 
to provide the best of both worlds.(If you are not familiar with using dagger, please take sometime to learn it, because it is a very good tool to reduce complexity, testing, and other software development merits)

### Different use cases:
1. If you just simply want to play a video without any interruption, simply use **MediaModel.video(name, VIDEO_URL, artwork, subtitle)** to build a mediaModel instance, then pass it in as intent extra with key **TubiPlayerActivity.TUBI_MEDIA_KEY** to start 
**DoubleViewTubiPlayerActivity**, then the code start a self contained activity to handle your playback experience 

2. In addition to play a video, if you also want to implement pre-roll, and middle-roll video ads, then there are more steps needed:
    1. You need to know at which positions to fetch for ads, and how to fetch ads, this is been handled by **AdInterface**, you need to implement your own logic.
    2. 


