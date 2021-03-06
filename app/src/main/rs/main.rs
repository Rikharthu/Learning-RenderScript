// Needed directive for RS to work
#pragma version(1)

// The java_package_name directive needs to use your Activity's package path
#pragma rs java_package_name(com.example.uberv.learningrenderscript)

// Creates a custom structure
typedef struct MyElement {

    int x;
    int y;
    bool simpleBool;

} MyElement_t;

// To get a custom element from an allocation:
// MyElement_t el = *(MyElement_t *)rsGetElementAt(aIn, index);
// To change custom element member:
// el.x = 10;
// To set a custom element in an allocation:
// rsSetElementAt(mAlloc, (void *)&el);

// Kernel that fills Allocation with some data
MyElement_t __attribute__((kernel)) initializeMyElements(uint32_t x) {
    MyElement_t el;

    el.x = x;
    el.y = x + 2;
    el.simpleBool = x % 2 == 0;

    return el;
}

// Kernel that debugs Allocation
void __attribute__((kernel)) debugAllocation(MyElement_t in) {

    rsDebug("Element", in.x, in.y, in.simpleBool);

}

void kernelFunctionName(const uchar2 * v_in, float * v_out, uint32_t x, uint32_t y){

}