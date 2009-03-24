using System;
using System.Data;
using System.Runtime.InteropServices;
using System.ComponentModel;

namespace dZine.Zineon
{

/// <summary>
/// The Memory class contains functions to map objects on unmanaged
/// memory.
/// </summary>
public class Memory
{
    public static IntPtr stringToPtr(string s)
    {
        IntPtr hLocal;
        try
        {
            hLocal = CoreDll.LocalAlloc( CoreDll.LPTR , (uint)(2 * (1 + s.Length)) );
        }
        catch(Win32Exception ex) 
        { 
            Console.WriteLine( "Memory.StringToPtr: An error occured while allocating memory: {1}", ex.Message );
            return IntPtr.Zero;
        }

        Marshal.Copy( s.ToCharArray() , 0 , hLocal , s.Length );
        return hLocal;
    }

    public static string ptrToString(IntPtr ptr, int length)
    {
        
        char[] buffer = new char[length];
        Marshal.Copy( ptr , buffer , 0 , length);
        return new string(buffer);       
    }
}

}
