const esbuild = require('esbuild');
const glob = require('glob');
const path = require('path');
const fs = require('fs');

// Work on the assembled webapp target directory
const targetDir = 'target/ResourceServingWebapp/';
if (!fs.existsSync(targetDir)) {
  console.log('Target directory not found, skipping minification');
  process.exit(0);
}

console.log(`Working in target directory: ${targetDir}`);

// Get all JS and CSS files in target, excluding YUI compressor excludes
const allJsFiles = glob.sync(`${targetDir}rs/**/*.js`, {
  ignore: [
    '**/*.min.js',
    '**/rs/ckeditor/**',
    '**/rs/datatables/**',
    '**/rs/jquery/2.2.4/**',
    '**/rs/jquery/3.2.1/**'
  ]
});

const allCssFiles = glob.sync(`${targetDir}rs/**/*.css`, {
  ignore: [
    '**/*.min.css',
    '**/rs/ckeditor/**',
    '**/rs/datatables/**',
    '**/rs/jquery/2.2.4/**',
    '**/rs/jquery/3.2.1/**'
  ]
});

// Filter to only files that don't already have a .min version
const jsFiles = allJsFiles.filter(file => {
  const minFile = file.replace(/\.js$/, '.min.js');
  return !fs.existsSync(minFile);
});

const cssFiles = allCssFiles.filter(file => {
  const minFile = file.replace(/\.css$/, '.min.css');
  return !fs.existsSync(minFile);
});

async function minifyFiles() {
  console.log(`Minifying ${jsFiles.length} JavaScript files and ${cssFiles.length} CSS files...`);
  
  // Process JavaScript files
  for (const file of jsFiles) {
    try {
      // Use safe minification that preserves object property access patterns
      // Disable identifier mangling to prevent breaking property access like layout.navigation.tabs.externalId
      const result = await esbuild.build({
        entryPoints: [file],
        minify: true,
        minifyWhitespace: true,
        minifyIdentifiers: false, // Disable to prevent breaking object property access
        minifySyntax: true,
        write: false
      });
      
      const minFile = file.replace(/\.js$/, '.min.js');
      fs.writeFileSync(minFile, result.outputFiles[0].text);
      console.log(`Minified: ${path.relative(targetDir, file)} -> ${path.relative(targetDir, minFile)}`);
    } catch (error) {
      console.warn(`Failed to minify ${file}: ${error.message}`);
      // Copy original file as fallback
      const minFile = file.replace(/\.js$/, '.min.js');
      fs.copyFileSync(file, minFile);
    }
  }
  
  // Process CSS files
  for (const file of cssFiles) {
    try {
      const result = await esbuild.build({
        entryPoints: [file],
        minify: true,
        write: false
      });
      
      const minFile = file.replace(/\.css$/, '.min.css');
      fs.writeFileSync(minFile, result.outputFiles[0].text);
      console.log(`Minified: ${path.relative(targetDir, file)} -> ${path.relative(targetDir, minFile)}`);
    } catch (error) {
      console.warn(`Failed to minify ${file}: ${error.message}`);
      // Copy original file as fallback
      const minFile = file.replace(/\.css$/, '.min.css');
      fs.copyFileSync(file, minFile);
    }
  }
  
  console.log('Minification complete!');
}

minifyFiles().catch(console.error);