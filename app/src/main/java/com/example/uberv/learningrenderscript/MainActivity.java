package com.example.uberv.learningrenderscript;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Type;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.debug_tv)
    TextView mDebugTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        example();
        renderscriptElementsDemo();
        renderscriptTypesDemo();
    }

    private void example() {
        // Instantiates the RenderScript context.
        RenderScript rs = RenderScript.create(this);

        // Create an input array, containing some numbers.
        int inputArray[] = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        // Instantiates the input Allocation, that will contain our sample numbers
        Allocation inputAllocation = Allocation.createSized(
                rs,
                Element.I32(rs),   // defines a type of one item in an allocation (roughly equivalent to C type)
                inputArray.length);
        // Copies the input array into the input Allocation.
        inputAllocation.copyFrom(inputArray);

        // Instantiates the output Allocation, that will contain the result of the process.
        Allocation outputAllocation = Allocation.createSized(rs, Element.I32(rs),
                inputArray.length);

        // Instantiates the sum script.
        ScriptC_sum myScript = new ScriptC_sum(rs);

        // Run the sum process, taking the elements belonging to the inputAllocation and placing
        // the process results inside the outputAllocation.
        myScript.forEach_sum2(inputAllocation, outputAllocation);

        // RenderScript context must be released when no longer needed
        rs.destroy();

        // Copies the result of the process from the outputAllocation to a simple int array.
        int outputArray[] = new int[inputArray.length];
        outputAllocation.copyTo(outputArray);

        String debugString = "Output: ";
        for (int i = 0; i < outputArray.length; i++)
            debugString += String.valueOf(outputArray[i]) + (i < outputArray.length - 1 ? "," : "");

        mDebugTv.setText(debugString);
    }

    private void renderscriptElementsDemo() {
        RenderScript rs = RenderScript.create(this);

        // An element represents one item within an Allocation.
        // It is roughly equivalent to a C type in a RenderScript kernel
        // You can use a predefined RS elements:
        Element int32 = Element.I32(rs);
        Element float32_2 = Element.F32_2(rs); // vector type with length = 2
        Element rgba_888 = Element.RGB_888(rs);
        // <type><size>_<vector_size>

        Timber.d(getElementInfo(int32));
        Timber.d(getElementInfo(float32_2));
        Timber.d(getElementInfo(rgba_888));

        // Inside RS scripts plain elements act as common vars: int value = 3;
        // while packed elements act as struct elements (internally defined using the ext_vector_type attribute
        /*
        // Definition:
        // typedef unsigned char uchar4 __attribute__((ext_vector_type(4)));
        uchar4 myChar4;
        myChar4.r = myChar4.x = myChar4.s0 = myChar4.S0;
        myChar4.g = myChar4.y = myChar4.s1 = myChar4.S1;
        myChar4.b = myChar4.z = myChar4.s2 = myChar4.S2;
        myChar4.a = myChar4.w = myChar4.s3 = myChar4.S3;
         */

        // Or you can create your own element, by creating a struct inside one of your scripts
        // Declared in renderscript/main.rs
        Element myElement = ScriptField_MyElement.createElement(rs);
        Allocation myElementsAllocation = Allocation.createSized(rs, myElement, 5);
        // Or
        // Allocation myElementsAllocation=ScriptField_MyElement.create1D(rs,sizeX).getAllocation;
        Timber.d(getElementInfo(myElement));

        // You can also create custom elements directly from the Java side by using Element.Builder
        Element.Builder elementBuilder = new Element.Builder(rs);
        elementBuilder.add(Element.I32(rs), "x");
        elementBuilder.add(Element.I32(rs), "y");
        elementBuilder.add(Element.F32(rs), "fx");
        elementBuilder.add(Element.F32(rs), "fy");
        Element myJavaElement = elementBuilder.create();
        Timber.d(getElementInfo(myJavaElement));

        rs.destroy();
    }

    /**
     * A RenderScript Type is the definition of the contents of an Allocation. It all starts with a Type.Builder
     * It describes the Element and dimensions used for an Allocation or a parallel operation.
     * A type always includes an Element and an X dimension. It may be multidimensional (up to 3 dimensions)
     */
    private void renderscriptTypesDemo() {
        RenderScript rs = RenderScript.create(this);

        // Example: I want to store a plain array of 54 integers
        // Using Type.Builder
        Type.Builder typeBuilder = new Type.Builder(rs, Element.I32(rs));
        typeBuilder.setX(54);   // add a dimension to the type
        Type type = typeBuilder.create();
        Allocation alloc = Allocation.createTyped(rs, type, Allocation.USAGE_SCRIPT);
        // Using Allocation.createSized:
        alloc = Allocation.createSized(rs, Element.I32(rs), 54);

        // Example: I want to store a 5-rows per 6-columns matrix of floats:
        typeBuilder = new Type.Builder(rs, Element.F32(rs));
        typeBuilder.setX(6);
        typeBuilder.setY(5);
        type = typeBuilder.create();
        alloc = Allocation.createTyped(rs, type, Allocation.USAGE_SCRIPT);
        // Or:
        type = Type.createXY(rs, Element.F32(rs), 6, 5); // from api 21

        // Example: I want to store an array of 5 custom Java elements, composed of 2 integers and 2 floats
        Element.Builder elementBuilder = new Element.Builder(rs);
        elementBuilder.add(Element.I32(rs), "x");
        elementBuilder.add(Element.I32(rs), "y");
        elementBuilder.add(Element.F32(rs), "fx");
        elementBuilder.add(Element.F32(rs), "fy");

        // Define my element
        Element myElement = elementBuilder.create();

        typeBuilder = new Type.Builder(rs, myElement);
        typeBuilder.setX(5);
        type = typeBuilder.create();
        alloc = Allocation.createTyped(rs, type, Allocation.USAGE_SCRIPT);
        // OR, more straightforwardly
        alloc = Allocation.createSized(rs, myElement, 5);

        // Example: I want to store an array of 9 custom struct elements, composed of 2 integers and one bool
        // Declares a new Allocation, based upon the custom struct Element
        myElement = ScriptField_MyElement.createElement(rs);
        typeBuilder = new Type.Builder(rs, myElement);
        typeBuilder.setX(9);
        type = typeBuilder.create();
        alloc = Allocation.createTyped(rs, type, Allocation.USAGE_SCRIPT);
        // OR, more straightforwardly
        alloc = Allocation.createSized(rs, myElement, 9);

        rs.destroy();
    }

    private String getElementInfo(Element element) {
        StringBuilder stringBuilder = new StringBuilder();

        // Encodes C type information of an Element
        Element.DataType type = element.getDataType();
        // Encodes how that Element should be interpreted by a Sampler
        // DataKing of "USER" cannot be used with a sampler (they should be bitmap-derived such as RGBA_8888)
        Element.DataKind kind = element.getDataKind();

        stringBuilder.append("Type:\t\t" + type.name() + "\n");
        stringBuilder.append("Size:\t\t" + element.getBytesSize() + " bytes" + "\n");
        stringBuilder.append("Vector size:\t" + element.getVectorSize() + "\n");
        stringBuilder.append("Kind:\t\t" + kind.name() + "\n");

        return stringBuilder.toString();
    }
}
