# 自定义纵向的LinearLayout
    用于学习自定义ViewGroup，OnMeasure OnLayout过程,抛去了所有自定义属性，只保留View的padding和margin
---
## OnMeasure 测量方法
    先说思路，纵向的LinearLayout测量，宽度和高度分别有match_parent，wrap_content两种情况。
    1·宽度、高度match_parent，对应精准模式 MeasureSpec.EXACTLY，直接设置宽高值
    2.宽度warp_content，需要测量所有子View的宽度，找出所有子View中，宽度最大的值，为LinearLayout的宽度。
    3. 高度warp_content，需要测量所有子View的高度，所有子view的高度和为LinearLayout的高度。
    4. 注意计算LinearLayout的padding和子View的margin

    贴代码：
    ~~~
    @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            switch (widthMode) {
                case MeasureSpec.AT_MOST:
                    // 一般当childView设置其宽、高为wrap_content时
                    break;
                case MeasureSpec.EXACTLY:
                    // 精准值模式，一般当childView设置其宽、高为精确值、match_parent时
                    mWidth = widthMeasureSpec;
                    break;
                case MeasureSpec.UNSPECIFIED:
                    //表示子布局想要多大就多大，一般出现在AadapterView的item
                    break;
            }

            switch (heightMode) {
                case MeasureSpec.AT_MOST:
                    // 一般当childView设置其宽、高为wrap_content时
                    break;
                case MeasureSpec.EXACTLY:
                    // 精准值模式，一般当childView设置其宽、高为精确值、match_parent时
                    heightMode = heightMeasureSpec;
                    break;
                case MeasureSpec.UNSPECIFIED:
                    //表示子布局想要多大就多大，一般出现在AadapterView的item
                    break;
            }
            int count = getChildCount();
            // 如果没有子View，不进行测量
            if (count <= 0) {
                setMeasuredDimension(mWidth, mHeight);
                return;
            }

            int childWidth = 0, childHeight = 0;
            mHeight = 0;
            for (int position = 0; position < count; position++) {
                View childView = getChildAt(position);
                // 测量子view
                measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
                if (widthMode != MeasureSpec.EXACTLY) {
                    // 开启自动测量宽度
                    int childMarginLeft = layoutParams.leftMargin;
                    int childMarginRight = layoutParams.rightMargin;
                    childWidth = childView.getMeasuredWidth() + childMarginLeft + childMarginRight + getPaddingLeft() + getPaddingRight();
                    // 取子View中，宽度最大的作为View的宽度
                    if (childWidth > mWidth) {
                        if (childWidth > screenWidth) {
                            mWidth = screenWidth;
                        } else {
                            mWidth = childWidth;
                        }
                    }
                }

                if (heightMode != MeasureSpec.EXACTLY) {
                    // 开启自动测量高度
                    childHeight = childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
                    mHeight += childHeight;
                }
            }

            setMeasuredDimension(mWidth, mHeight + getPaddingTop() + getPaddingBottom());

        }
    ~~~




###  测量的三种模式
        ~~~
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                switch (widthMode) {
                    case MeasureSpec.AT_MOST:
                        // 一般当childView设置其宽、高为wrap_content时
                        break;
                    case MeasureSpec.EXACTLY:
                        // 精准值模式，一般当childView设置其宽、高为精确值、match_parent时
                        mWidth = widthMeasureSpec;
                        break;
                    case MeasureSpec.UNSPECIFIED:
                        //表示子布局想要多大就多大，一般出现在AadapterView的item
                        break;
                }
        ~~~

    注释很清楚，不解释了

### 测量子View
    // 测量子View，测量的宽和高，抛去了margin
    measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
    // 测量子View，测量的宽和高包含margin值
    measureChild(childView,widthMeasureSpec,heightMeasureSpec);

    这里为了方便使用padding和margin，使用 measureChildWithMargins 来测量，获取到view的净宽高
    测量完后，计算View的宽度和高度时，还需要手动加上margin
    childWidth = childView.getMeasuredWidth() + childMarginLeft + childMarginRight + getPaddingLeft() + getPaddingRight();
    childHeight = childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

---
## OnLayout  布局方法
    OnLayout 方法，就是通过childView.layout(l,t,r,b),对子view进行布局
    思路也比较简单，计算左上角和右下角坐标点，抛去padding和margin的话，第一次左上角为 （0,0）点，右下角为（childWidth，childHeight）
    第二次 左上角坐标为(0,oldHeight),右下角为(childWidht,oudHeight+childHeight)
    以此类推
    ...
     实际上，还要加上padding和margin的计算
     代码比较简单，直接贴了
    ~~~
     @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int count = getChildCount();
            if (count <= 0) {
                return;
            }
            int left = 0, top = getPaddingTop(), right, bottom;
            View childView;
            for (int position = 0; position < count; position++) {
                childView = getChildAt(position);
                LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
                left = layoutParams.leftMargin + getPaddingLeft();
                top += layoutParams.topMargin;
                right = childView.getMeasuredWidth() + getPaddingRight() + layoutParams.leftMargin;
                bottom = childView.getMeasuredHeight() + top;
                childView.layout(left, top, right, bottom);
                top += childView.getMeasuredHeight() + layoutParams.bottomMargin;
            }
        }
    ~~~

### LayoutParams 的使用
    上面代码中，需要获取子view的margin值，所以要给我们LinearLayout配置LayoutParams才能获取到margin
    配置方法也很简单，自定义一个LayoutParams继承ViewGroup.MarginLayoutParams
    重写ViewGroup 的generateLayoutParams方法，返回我们自定义的LayoutParams即可

    自定义LayoutParams 代码：
    ~~~
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {


            public LayoutParams(Context c, AttributeSet attrs) {
                super(c, attrs);
            }

            public LayoutParams(int width, int height) {
                super(width, height);
            }

            public LayoutParams(MarginLayoutParams source) {
                super(source);
            }

            public LayoutParams(ViewGroup.LayoutParams source) {
                super(source);
            }
        }
    ~~~

    绑定自定义的LayoutParams：
    ~~~
     @Override
        public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
            return new LayoutParams(getContext(), attrs);
        }
    ~~~















