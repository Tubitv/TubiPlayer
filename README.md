# TubiPlayer
The official android player of [Tubi TV](https://www.tubi.tv) -- A free movie and TV streaming service.

## Purposes of TubiPlayer
In the industry of streaming free multi-media contents to users, companies choose to show in-stream video ads to users between the main content as their business model. Examples include **YouTube** and **Tubi TV**.

From the technology side of things, in order to implement an android player that streams multiple media very well, switches between movies and ads dynamically, and presents users with a seamless user experience, it requires a series of stateful actions that poses a lot of complexities for enginners to manage. **TubiPlayer offers an industry standard, multi-media streaming and ad-supported solution on the Android platform, with highly customized code architecture and implementation**

## What Does TubiPlayer Do Well.
* Native multi-media streaming on Android
* Industry standard Ad solutions, including VAST and VPAID
* Error-handing
* Seamless UX 
* Highly customized module


## Dependency Management
In order to provide highly customized code base, we have leveraged [Dagger's](https://github.com/google/dagger) dependency management framework to better manage dependencies for your own business logic.(If you are not familiar with using dagger, please take sometime to learn it, because it is a very good tool to reduce complexity, testing, and other software development merits)
In general, your business requirement may require different logic of showing video ads at different positions, and different types of ads, and so forth. The idea behind Tubiplayer's dependency management is that we provide the basic classes and functionality
to implement playback experience, and if you have different logic, you just override that particular class or functionality without the need to thoroughly understand the whole code base. As the result, you are able to easily and quickly use TubiPlayer without invest much time 
upfront.

Nearly every business related dependencies are managed by [PlayerModuleDefault](./lib/src/main/java/com/tubitv/media/di/PlayerModuleDefault.java), where all the dependencies are being instantiated and being injected into [DoubleViewTubiPlayerActivity](./lib/src/main/java/com/tubitv/media/activities/DoubleViewTubiPlayerActivity.java)
You can choose to use the default behavior by using the module out of box, but if you have customized business logic, you can directly change the dependencies in ***PlayerModuleDefault*** for customization

## How to use 
Different application often requires different sets of rules and logic to satisfy business requirements, as the result, Tubiplayer was build to provide high degree of
customization. However, highly customized code if not managed well can quickly turn into nightmare. Therefore, Tubiplayer leverages a third party Dependency Injection framework:[Dagger](https://github.com/google/dagger) 
to provide the best of both worlds.(If you are not familiar with using dagger, please take sometime to learn it, because it is a very good tool to reduce complexity, testing, and other software development merits)

### Different use cases:
1. If you just simply want to play a video without any interruption, you can just use ***DoubleViewTubiPlayerActivity*** out of box, then the code start a self contained activity to handle your playback experience
```java
      String subs = "http://put_your_own_subtitle.srt";
                 String artwork = "http://www.put_your_own_art_work.png";
                 String name = "Example Video";
                 String video_url = "http://put_your_own_video_url.mp4";
                 Intent intent = new Intent(SelectionActivity.this, DoubleViewTubiPlayerActivity.class);
                 intent.putExtra(TubiPlayerActivity.TUBI_MEDIA_KEY, MediaModel.video(name, video_url, artwork, null));
                 startActivity(intent);
```

2. In addition to play a video, if you also want to implement pre-roll, and middle-roll video ads, then there are more steps needed:
    1. You need to know at which positions to fetch for ads, and how to fetch ads, this is been handled by **AdInterface**, you need to implement your own logic.
    2. 



## Player Lifecycle
The ExoPlayer needs to create and release resources that binds to Activity's lifecycle, and also you may need to do some customized code logic in player's lifecycle.
![State Machine](./documentation/tubiplayer_lifecycle.png)

## State Machine Diagram
The business logic of switching ads are complicated and stateful, in order to provide better state management, this diagrams below demonstrate the state transitions and you can use 
this diagram to add your customized logic
![State Machine](https://github.com/Tubitv/TubiPlayer/blob/master/lib/doc/Screen%20Shot%202017-09-18%20at%204.23.53%20PM.png)