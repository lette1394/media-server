### Requirements 1
- [storage] Support streaming: 10GB file up/download
- [storage] Support range upload (a.k.a 이어올리기) 
    - [x] Update size per object sub classes 
    - [x] exception callback when uploading is broken
    - [x] Add test for classes
    - [x] Adjust class access modifier level
    - [ ] Add netty, spring mvc, spring webflux implements
 
 
### Requirements 2
- [storage] Support content-type 

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


### ideas
- java generic은 쉽지 않다...
- interface 계층구조에는 적합하지 않다-> 애초에 이런 의도가 아닌듯. 어떤 클래스가 뭐가 구현될지 모르니까 음...
- sync, async 두 방식을 모두 지원하면서 generic type까지 가져가는 유일한 방법은; sync-object, async-object 각각 나누는 것 밖에 없는 듯...
- 후.. 근데 sync/async 관계 없이 로직 흐름을 그대로 따라가고 싶은데 그러면 코드 중복을 어떻게 제거하지...
