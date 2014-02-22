Why did I fork Picasso?
-----------------------

This is not just some random fork of Picasso. Inspired by Romain Guy's post (www.curious-creature.org/2012/12/11/android-recipe-1-image-with-rounded-corners/) about rounded corners for images, I tried to extend Picasso in order to support this particular functionality. I've added two simple methods to the constructor:

    Picasso
        .with(context)
        .load(url)
        .round()                    // the image gets rounded corners
        .setBorder(15, Color.RED)   // the image gets a selection border, with the specified size/color
        .fit()
        .into(view);

I will also add a method that will specify the corner radius.

Picasso
=======

A powerful image downloading and caching library for Android

![](website/static/sample.png)

For more information please see [the website][1]



Download
--------

Download [the latest JAR][2] or grab via Maven:

```xml
<dependency>
    <groupId>com.squareup.picasso</groupId>
    <artifactId>picasso</artifactId>
    <version>(insert latest version)</version>
</dependency>
```


ProGuard
--------

If you are using ProGuard make sure you add the following option:

```
-dontwarn com.squareup.okhttp.**
```



License
--------

    Copyright 2013 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [1]: http://square.github.io/picasso/
 [2]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.squareup.picasso&a=picasso&v=LATEST
