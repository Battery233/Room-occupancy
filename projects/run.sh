if (mbed compile -t GCC_ARM -m NRF51_DK) then
  cp ./BUILD/NRF51_DK/GCC_ARM/${PWD##*/}.hex /run/media/$USER/DAPLINK
  mbed detect; # calling this has the effect of blocking until cp is finished
  perl -e 'use POSIX; tcsendbreak(3, 0)' 3>/dev/ttyACM0
  if ! screen /dev/ttyACM0 115200 ; then
    screen -r 
  fi
else
  echo "Compile Failed"
fi

