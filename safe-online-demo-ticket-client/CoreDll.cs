using System;
using System.Data;
using System.Runtime.InteropServices;
using System.ComponentModel;

namespace dZine.Zineon
{
    /// <summary>
    /// The CoreDll class acts as a wrapper for the coredll.dll library 
    /// It also has a function to convert a String to a pointer.
    /// </summary>
	public class CoreDll
	{
        public const uint LMEM_FIXED = 0x0000;
        public const uint LMEM_MOVEABLE = 0x0020;
        public const uint LMEM_ZEROINIT = 0x0040;
        public const uint LHND = (LMEM_MOVEABLE | LMEM_ZEROINIT);
        public const uint LPTR = (LMEM_FIXED | LMEM_ZEROINIT);
        public const uint NONZEROLHND = LMEM_MOVEABLE;
        public const uint NONZEROLPTR = LMEM_FIXED;

        public const uint INVALID_HANDLE_VALUE = 0xFFFFFFFF;

        /// <summary>
        /// Creation Dispostions for CreateFile
        /// </summary>
        public const uint OPEN_EXISTING = 0x00000003;
        /* defined in winnt.h */
        public const uint FILE_ATTRIBUTE_NORMAL = 0x00000080;
        //
        //  These are the generic rights.
        //
        public const uint GENERIC_READ = 0x80000000;
        public const uint GENERIC_WRITE = 0x40000000;
        public const uint GENERIC_EXECUTE = 0x20000000;
        public const uint GENERIC_ALL = 0x10000000;

        //
        // Define the method codes for how buffers are passed for I/O and FS controls
        // (winioctl.h)
        //
        public const uint METHOD_BUFFERED = 0;
        public const uint METHOD_IN_DIRECT = 1;
        public const uint METHOD_OUT_DIRECT = 2;
        public const uint METHOD_NEITHER = 3;

        //
        // Define the access check value for any access
        //
        //
        // The FILE_READ_ACCESS and FILE_WRITE_ACCESS constants are also defined in
        // ntioapi.h as FILE_READ_DATA and FILE_WRITE_DATA. The values for these
        // constants *MUST* always be in sync.
        //
        // (winioctl.h)
        public const uint FILE_ANY_ACCESS = 0;
        public const uint FILE_READ_ACCESS = 0x0001;    // file & pipe
        public const uint FILE_WRITE_ACCESS = 0x0002;    // file & pipe

        //
        // Windows CE Specific Defines
        //
        // (winioctl.h)
        public const uint FILE_DEVICE_HAL = 0x00000101;
        public const uint FILE_DEVICE_CONSOLE = 0x00000102;
        public const uint FILE_DEVICE_PSL = 0x00000103;
        public const uint FILE_DEVICE_SERVICE = 0x00000104;
        public const uint FILE_DEVICE_NLED = 0x00000105;
        public const uint FILE_DEVICE_NOTIFY = 0x00000106;
        
        // source: winsmcrd.h
        public const uint FILE_DEVICE_SMARTCARD = 0x00000031;

        /* helper function to create ioctl codes */
        /* (winioctl.h) */
        public static uint CTL_CODE(uint DeviceType, uint Function, uint Method, uint Access )
        {
            return ((DeviceType) << 16) | ((Access) << 14) | ((Function) << 2) | (Method);
        }


        #region DllImports

        [DllImport("coredll.dll", EntryPoint="LocalAlloc", SetLastError=true)]
        private static extern IntPtr LocalAllocUnsafe(UInt32 uFlags, UInt32 uBytes);

        [DllImport("coredll.dll",EntryPoint="LocalFree", SetLastError=true)]
        private static extern IntPtr LocalFreeUnsafe(IntPtr hMem);

        [DllImport("coredll.dll", EntryPoint="LocalReAlloc", SetLastError=true)]
        private static extern IntPtr LocalReAllocUnsafe(IntPtr hMem, UInt32 uBytes, UInt32 uFlags);

        [DllImport("coredll.dll", EntryPoint="CreateFile", SetLastError=true)]
        private static extern IntPtr CreateFileUnsafe
        (
            String lpFileName, 
            UInt32 dwDesiredAccess, 
            UInt32 dwShareMode,
            IntPtr lpSecurityAttributes /* altijd null */, 
            UInt32 dwCreationDispostion, 
            UInt32 dwFlagsAndAttributes, 
            IntPtr hTemplateFile
        ); 

        [DllImport("coredll.dll", EntryPoint="CloseHandle", SetLastError=true)]
        private static extern int CloseHandleUnsafe( IntPtr hObject);

        [DllImport("coredll.dll", EntryPoint="DeviceIoControl", SetLastError=true)]
        private static extern Int32 DeviceIoControlUnsafe
        (
            IntPtr hDevice, 
            UInt32 dwIoControlCode, 
            byte[] lpInBuffer, 
            UInt32 nInBufferSize, 
            byte[] lpOutBuffer, 
            UInt32 nOutBufferSize, 
            out int lpBytesReturned, 
            IntPtr lpOverlapped
        );

        [DllImport("coredll.dll", EntryPoint="GetLastError", SetLastError=true)]
        public static extern Int32 GetLastError();

        #endregion

        public static IntPtr LocalAlloc(uint uFlags, uint uBytes)
        {
            IntPtr hLocal = LocalAllocUnsafe(uFlags, uBytes);
            if( hLocal.Equals(IntPtr.Zero) ) throw new Win32Exception( Marshal.GetLastWin32Error() );
            return hLocal;
        }

        public static void LocalFree(IntPtr hMem)
        {
            if( hMem.Equals(LocalFreeUnsafe(hMem)) ) throw new Win32Exception( Marshal.GetLastWin32Error() );
        }

        public static IntPtr LocalReAlloc(IntPtr hMem, uint uBytes, uint uFlags)
        {
            IntPtr hLocalNew = LocalReAllocUnsafe(hMem, uBytes, uFlags);
            if( hLocalNew.Equals(IntPtr.Zero) ) throw new Win32Exception( Marshal.GetLastWin32Error() );
            return hLocalNew;
        }

        
        public static IntPtr CreateFile( string lpFileName , 
                                         uint dwDesiredAccess , 
                                         uint dwShareMode,
                                         IntPtr lpSecurityAttributes /* altijd null */, 
                                         uint dwCreationDispostion, 
                                         uint dwFlagsAndAttributes, 
                                         IntPtr hTemplateFile )
        {
            IntPtr hLocal = CreateFileUnsafe( lpFileName, dwDesiredAccess, dwShareMode, lpSecurityAttributes, dwCreationDispostion, dwFlagsAndAttributes, hTemplateFile );
            if( (uint)hLocal == INVALID_HANDLE_VALUE ) throw new Win32Exception( Marshal.GetLastWin32Error() );
            return hLocal;
        }

        public static void CloseHandle( IntPtr hObject )
        {
            if( CloseHandleUnsafe(hObject) == 0 ) throw new Win32Exception( Marshal.GetLastWin32Error() );
        }

        public static int DeviceIoControl( IntPtr hDevice, 
                                            uint ioControlCode, 
                                            byte[] inBuffer, 
                                            uint inBufferSize, 
                                            byte[] outBuffer, 
                                            uint outBufferSize, 
                                            out int bytesReturned, 
                                            IntPtr overlapped )
        {                                           
            try
            {
                return DeviceIoControlUnsafe( hDevice, ioControlCode, inBuffer, inBufferSize, outBuffer, outBufferSize, out bytesReturned, overlapped );
            }
            catch(Exception){ bytesReturned = 0;  return -Marshal.GetLastWin32Error(); }
        }


      
	}
}
