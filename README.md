### Requirements 1
- [storage] Support streaming: 10GB file up/download
- [storage] Support range upload (a.k.a 이어올리기) 
    - [x] Update size per object sub classes 
    - [x] exception callback when uploading is broken
    - [x] Add test for classes
    - [ ] Adjust class access modifier level
    - [ ] Add netty, spring mvc, spring webflux implements
 
 
### Requirements 2
### Requirements 3
### Requirements 4


### Undefined 
- [processing] image/video thumbnail download
- [processing] image/video encoding
- [storage] support tag / metadata
- [processing] object handling pipeline 
- [delivery] cache layer
- [monitoring] up/download statistics 
- [security] object access policy  

...


---

jvm options to remove warnings
```
--add-opens
java.base/java.lang=ALL-UNNAMED
--add-opens
java.base/java.util=ALL-UNNAMED
--add-opens
java.base/java.util.function=ALL-UNNAMED
--add-opens
java.base/java.util.stream=ALL-UNNAMED
--add-opens
java.base/java.util.concurrent=ALL-UNNAMED
--add-opens
java.base/java.lang.reflect=ALL-UNNAMED
--add-opens
java.base/java.lang.invoke=ALL-UNNAMED
--add-opens
java.base/java.io=ALL-UNNAMED
```