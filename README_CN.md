Binding Layout
------

  [README](README.md)


**本插件仅适用于 Android Studio**.
>
快速为实体类创建用于 DataBinding 的 layout 布局


## 安装   
- Windows:
  - <kbd>File</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "BindingLayout"</kbd> > <kbd>Install Plugin</kbd>

- MacOs:
  - <kbd>Preferences</kbd> > <kbd>Settings</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "BindingLayout"</kbd> > <kbd>Install Plugin</kbd>

- 手动安装:
  - 下载 [Latest Release](https://github.com/leveychen/BindingLayout/releases) 
  >- <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Install plugin from disk...</kbd>
  - JetBrains Plugin [Download](http://plugins.jetbrains.com/plugin/10555)
  
**Restart IDE**.

## 用法
### 编译器

![ide.png](http://ww3.sinaimg.cn/large/0060lm7Tly1fpmulivs9tj309j05pwei.jpg)

![generate.png](http://ww2.sinaimg.cn/large/0060lm7Tly1fpmumr9wv7j306p06sjrd.jpg) 

###  快捷键

默认:  **Option + L**  (Mac), **Alt + L** (Windows)

#### 冲突或者自己习惯，去改快捷键吧

## Demo 

###  IDE
![before.png](http://ww1.sinaimg.cn/large/0060lm7Tly1fpo16c7vzzj30gw08ojrj.jpg
)

###  完成
创建的 layout 布局 和 getter,setter 
![after1.png](http://ww4.sinaimg.cn/large/0060lm7Tly1fpo1422di1j30ix0ll0u1.jpg)
![after3.png](http://ww3.sinaimg.cn/large/0060lm7Tly1fpo0tv130ej30lu0o1abw.jpg)
![after2.png](http://ww2.sinaimg.cn/large/0060lm7Tly1fpmvcuhdcgj30jr0ebq3u.jpg)



## 版本

v1.0.1
> 
* 支持自动生成构造器
* 支持继承BaseObservable的实体类

v1.0.0
> 
* 创建用于 data binding 的布局
* 自动生成 getter 和 setter (可配合 GsonFormat 使用,自动追加需要的方法与内容)


## 致谢
Special thanks to [GsonFormat](https://github.com/zzz40500/GsonFormat) and [DataBindingModelFormatter](https://github.com/Qixingchen/DataBindingModelFormatter)


## License

    Copyright 2018 LeveyChen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
