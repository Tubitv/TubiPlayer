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
customization. However, highly customized code if not managed well can quickly turn into nightmare. Therefore, Tubiplayer leverages a third party Dependency Injection framework: **Dagger** 
to provide the best of both worlds.(If you are not familiar with using dagger, please take sometime to learn it, because it is a very good tool to reduce complexity, testing, and other software development merits)

There is quick step by step use:
1. Currently, the whole playback experience is handled within an Activity: ***DoubleViewTubiPlayerActivity***, the idea is that you want to play some source,
you create a instance of ***MediaModel***, which includes information of the video, and you pass it in as a extra in intent, with the Key **TubiPlayerActivity.TUBI_MEDIA_KEY**

2.


