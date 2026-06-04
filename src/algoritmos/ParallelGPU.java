package algoritmos;

import org.jocl.*;
import static org.jocl.CL.*;

public class ParallelGPU {

    private static final String programSource =
            "__kernel void contarPalavra(__global const char* texto, " +
                    "                            const int tamanhoTexto, " +
                    "                            __global const char* palavra, " +
                    "                            const int tamanhoPalavra, " +
                    "                            __global int* resultado) { " +
                    "    int i = get_global_id(0); " +
                    "    if (i <= tamanhoTexto - tamanhoPalavra) { " +
                    "        bool match = true; " +
                    "        for (int j = 0; j < tamanhoPalavra; j++) { " +
                    "            if (texto[i + j] != palavra[j]) { " +
                    "                match = false; " +
                    "                break; " +
                    "            } " +
                    "        } " +
                    "        if (match) { " +
                    "            atomic_add(resultado, 1); " +
                    "        } " +
                    "    } " +
                    "}";

    public static long contar(String texto, String palavra) {
        CL.setExceptionsEnabled(true);

        byte[] textoBytes = texto.getBytes();
        byte[] palavraBytes = palavra.getBytes();
        int[] resultadoArray = new int[]{0};

        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        cl_platform_id platforms[] = new cl_platform_id[numPlatformsArray[0]];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        int numDevicesArray[] = new int[1];
        try {
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevicesArray);
        } catch (CLException e) {
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
        }

        cl_device_id devices[] = new cl_device_id[numDevicesArray[0]];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, devices.length, devices, null);
        cl_device_id device = devices[0];

        cl_context context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);
        cl_command_queue commandQueue = clCreateCommandQueueWithProperties(context, device, new cl_queue_properties(), null);

        cl_mem textoMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * textoBytes.length, Pointer.to(textoBytes), null);

        cl_mem palavraMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * palavraBytes.length, Pointer.to(palavraBytes), null);

        cl_mem resultadoMem = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int, Pointer.to(resultadoArray), null);

        cl_program program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, "contarPalavra", null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(textoMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{textoBytes.length}));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(palavraMem));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{palavraBytes.length}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(resultadoMem));

        long[] global_work_size = new long[]{textoBytes.length};
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, null, 0, null, null);

        clEnqueueReadBuffer(commandQueue, resultadoMem, CL_TRUE, 0, Sizeof.cl_int, Pointer.to(resultadoArray), 0, null, null);

        clReleaseMemObject(textoMem);
        clReleaseMemObject(palavraMem);
        clReleaseMemObject(resultadoMem);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        return resultadoArray[0];
    }
}