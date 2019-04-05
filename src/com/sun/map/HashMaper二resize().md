## resize()方法：
初始化或扩容方法。如果为null，会根据设置的阈值对目标进行分配。否则对容量进行扩容。resize()是非常耗时的，在编写过程中尽量避免。
```java
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            //如果超过了最大值就不再扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //如果没超过最大值，就扩充为原来的2倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1;
        }
        //更新阈值
        else if (oldThr > 0) 
            newCap = oldThr;
        //长度为0，对数组进行初始化
        else {               
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        //更细reszie上限
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            //将bucket中的元素移动到新的bucket中 
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    //如果桶中只有一个bin，就直接将他放到信标的目标位置
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    //如果时红黑树，ze拆分树
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else {  // 这一段在下面进行详解
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```
我们来单独看一下这段代码
#### resize()时链表的拆分
```java
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
```
#### 第一段
```java
       Node<K,V> loHead = null, loTail = null;
       Node<K,V> hiHead = null, hiTail = null;
```
从命名上我们可以猜出，这里定义了连个来链表==lo链表==,==hi链表==。
#### 第二段
```java
        do{
            next = e.next;
            ...
        }while((e = next)!=null);
        ...
```
```java
                               
                if ((e.hash & oldCap) == 0) {
                     //插入lo链表
                    if (loTail == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                }
                else {
                    //插入hi链表
                    if (hiTail == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                }
```
> 这里我们能可以看到如果<font color="red">(e.hash & oldCap) == 0)</font>我们就将节点插入lo链表，否则插入hi链表。
```java
        if (loTail != null) {
            loTail.next = null;
            newTab[j] = loHead;
       }
       if (hiTail != null) {
           hiTail.next = null;
           newTab[j + oldCap] = hiHead;
       }
```
> 如果lo链表非空，那么就把lo链表插入到新table的j位置
> 如果hi链表非空，那么就把hi链表擦汗如到table的j+oldCap 位置上

我们来仔细看一下 ==(e.hash & oldCap) == 0)== 和 ==j + oldCap==到底是什么意思。

首先我们要明确三点：
    1. oldCap一定是2的幂次倍。假设为2<sup>m</sup> 
    2. newCap一定是oldCap的2倍。则2<sup>m+1</sup>
    3. hash对数组取模(n-1)&hash.就是取hash值的低m位

假设：oldCap=16(2<sup>4</sup> )，neCap=32(2<sup>5</sup>) 

16-1=15 二进制表示为：==0000 0000 0000 0000 0000 0000 0000 1111==
(16-1)&hash自然就是取hash值的低四位。我们假设为==abcd==

扩大后，(32-1)&hash就是取hash值的低5位。对于同一个Node低5位的值无非就是两种情况
- ==0abcd==
- ==1abcd==
其中==0abcd==与原值相同。==1abcd== = ==0abcd+10000== = ==0abcd+oldCap==。

那新旧index是否一致就体现在hash值的第四位(最低为为0)
> hash & 0000 0000 0000 0000 0000 0000 0001 0000

上式等于
> hash & oldCap

所以我们得出结论
>如果<font color="red">(e.hash & oldCap) == 0</font> 则该节点在新旧表的下标位置相同 ==j==
>如果<font color="red">(e.hash & oldCap) == 1</font> 则该节点在新表的下标位置==j+oldCap==




