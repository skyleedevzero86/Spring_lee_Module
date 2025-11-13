const fs = require('fs');
const path = require('path');

const dirsToClean = [
  path.join(__dirname, '..', '.next'),
  path.join(__dirname, '..', 'node_modules', '.cache'),
];

dirsToClean.forEach((dir) => {
  if (fs.existsSync(dir)) {
    fs.rmSync(dir, { recursive: true, force: true });
    console.log(`삭제됨: ${dir}`);
  }
});

console.log('캐시 정리 완료');

